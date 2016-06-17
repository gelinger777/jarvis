package eventstore.tools.net

import com.amazonaws.auth.SystemPropertiesCredentialsProvider
import com.amazonaws.regions.Region
import com.amazonaws.regions.Regions
import com.amazonaws.regions.Regions.US_WEST_2
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.iterable.S3Objects
import com.amazonaws.services.s3.model.S3ObjectSummary
import com.amazonaws.services.s3.transfer.TransferManager
import eventstore.tools.internal.fileName
import util.global.*
import util.misc.RefCountRepeatingTask
import java.io.File
import java.util.concurrent.TimeUnit.MINUTES

/**
 * Polls data from remote data storage.
 */
class QueuePoller(
        val localPath: String,
        val remotePath: String,
        val bucket: String,
        val region: Regions = US_WEST_2) {
    private val log = logger("EventStreamPoller")

    val s3: AmazonS3Client

    val task = RefCountRepeatingTask(
            name = "event-stream-poller",
            task = {
                // no failures are accepted
                executeMandatory { this.check() }
            },
            //            delay = 2000
            delay = 5, unit = MINUTES
    )

    init {
        // validate s3
        s3 = AmazonS3Client(executeAndGetMandatory { SystemPropertiesCredentialsProvider().credentials })
        s3.setRegion(Region.getRegion(region))
        condition(s3.listBuckets().map { it.name }.contains(bucket), "bucket '$bucket' does not exist")

        log.info { "starting ${task.name}" }
        task.forceStart()
    }

    private fun check() {
        log.debug { "${task.name} : checking" }

        val localRoot = File(localPath)

        if (!localRoot.exists() || !localRoot.isDirectory) return

        // collect data about local files
        val localFiles = localRoot.listFiles().map { it.name }

        // collect data about remote files
        val remoteFiles = S3Objects.inBucket(s3, bucket)
                .filter { it.key.startsWith("$remotePath/") && it.key.endsWith(".cq4") }.map { it }.toList()

        val tm = TransferManager(s3)

        // download all remote files not found in local file system
        remoteFiles
                .filter {
                    localFiles.notContains(it.fileName()).apply {
                        if (this == false) log.trace { "skipping ${it.key} as it is already available locally" }
                    }
                }
                .forEach { download(it, tm) }

        tm.shutdownNow(false)
    }

    private fun download(s3object: S3ObjectSummary, tm: TransferManager) {

        val file = File("$localPath/${s3object.fileName()}")

        log.info { "initiating download from s3:$bucket/${s3object.key} to ${file.path}" }

        val download = tm.download(bucket, s3object.key, file)

        sleepLoopUntil(
                condition = { download.isDone },
                block = {
                    log.info { "${download.description} (${download.state})" }
                    log.debug { "progress : ${download.progress.percentTransferred.roundDown2()} %  (${size(download.progress.bytesTransferred)})" }
                },
                delay = 1000
        )

        log.info { "successfully downloaded : ${file.name}" }
    }

}