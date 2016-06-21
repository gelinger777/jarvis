package eventstore.tools.net

import com.amazonaws.auth.SystemPropertiesCredentialsProvider
import com.amazonaws.regions.Region
import com.amazonaws.regions.Regions
import com.amazonaws.regions.Regions.US_WEST_2
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.iterable.S3Objects
import com.amazonaws.services.s3.transfer.TransferManager
import eventstore.tools.internal.fileName
import util.global.*
import util.misc.RefCountRepeatingTask
import java.io.File
import java.util.concurrent.TimeUnit.MINUTES

/**
 * Tracks the stream for rollups and uploads them to the AWS S3 storage, credentials must be available via system properties...
 */
class QueueUploader(
        val localPath: String,
        val remotePath: String,
        val bucket: String,
        val region: Regions = US_WEST_2,
        val delay : Long = MINUTES.toMillis(5)
) {
    private val log = logger("EventStreamUploader")

    val s3: AmazonS3Client

    val task = RefCountRepeatingTask(
            name = "event-stream-uploader",
            task = {
                // no failures are accepted
                executeMandatory { this.check() }
            },
            delay = delay
    )

    init {
        // validate s3
        s3 = AmazonS3Client(executeAndGetMandatory { SystemPropertiesCredentialsProvider().credentials })
        s3.setRegion(Region.getRegion(region))
        condition(s3.listBuckets().map { it.name }.contains(bucket), "bucket '$bucket' does not exist")

        log.info { "starting uploader : from '$localPath' to 's3:$bucket/$remotePath' with ${duration(delay)} delay on each check" }
        task.forceStart()
    }

    private fun check() {
        log.debug { "${task.name} : checking" }

        val localRoot = File(localPath)

        if (!localRoot.exists() || !localRoot.isDirectory) {
            log.debug { "local source $localRoot is not valid skipping" }
            return
        }

        // collect data about local files
        val localFiles = localRoot.listFiles()
        val lastModified = localRoot.listFiles().map { it.lastModified() }.max() ?: return

        // collect data about remote files
        val remoteFiles = S3Objects.inBucket(s3, bucket)
                .filter { it.key.startsWith("$remotePath/") && it.key.endsWith(".cq4") }.map { it.fileName() }.toList()

        val tm = TransferManager(s3)

        // upload all local files that are not uploaded except the currently used one
        localFiles
                .filter {
                    (it.lastModified() < lastModified).apply {
                        if (this == false) log.trace { "skipping ${it.name} as it is the current queue" }
                    }
                }
                .filter {
                    (remoteFiles.notContains(it.name)).apply {
                        if (this == false) log.trace { "skipping ${it.name} is already uploaded" }
                    }
                }
                .forEach { upload(it, tm) }

        tm.shutdownNow(false)
    }

    private fun upload(file: File, tm: TransferManager) {
        val destination = "$remotePath/${file.name}"

        log.info { "initiating upload from ${file.path} to s3:$bucket/$destination" }

        val upload = tm.upload(bucket, destination, file)

        sleepLoop(
                condition = { upload.isDone },
                task = {
                    log.info { "${upload.description} (${upload.state})" }
                    log.debug { "progress : ${upload.progress.percentTransferred.roundDown2()} %  (${size(upload.progress.bytesTransferred)})" }
                },
                delay = MINUTES.toMillis(1)
        )

        log.info { "successfully uploaded : ${file.name}" }

        file.delete()

        log.info { "removed local ${file.name}" }
    }

}
