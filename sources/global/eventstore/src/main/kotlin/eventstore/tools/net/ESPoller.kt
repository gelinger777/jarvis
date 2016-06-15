package eventstore.tools.net

import com.amazonaws.auth.SystemPropertiesCredentialsProvider
import com.amazonaws.regions.Region
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.iterable.S3Objects
import com.amazonaws.services.s3.model.S3ObjectSummary
import com.amazonaws.services.s3.transfer.TransferManager
import eventstore.tools.internal.fileName
import eventstore.tools.internal.isChronicleFile
import util.global.*
import util.misc.RefCountRepeatingTask
import java.io.File
import java.util.concurrent.TimeUnit.MINUTES

/**
 * Polls data from remote data storage.
 */
class ESPoller(
        val localRoot: String,
        val bucket: String = "jarvis-historical",
        val folder: String,
        val region: Regions = Regions.AP_SOUTHEAST_1) {
    private val log = logger("ESPoller")

    val s3: AmazonS3Client

    val task = RefCountRepeatingTask(
            name = "es-poller",
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
    }

    fun start() {
        log.info { "starting ${task.name}" }
        task.forceStart()
    }

    fun stop() {
        log.info { "stopping ${task.name}" }
        task.forceStop()
    }

    private fun check() {
        log.debug { "${task.name} : checking" }

        val localRoot = File(localRoot)

        if (!localRoot.exists() || !localRoot.isDirectory) return

        // collect data about local files
        val localFiles = localRoot.listFiles().map { it.name }

        // collect data about remote files
        val remoteFiles = S3Objects.inBucket(s3, bucket)
                .filter { it.isChronicleFile() }.map { it }.toList()

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

        val file = File("$localRoot/${s3object.fileName()}")

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