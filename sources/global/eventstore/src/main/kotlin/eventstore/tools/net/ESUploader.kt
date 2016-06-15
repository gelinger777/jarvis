package eventstore.tools.net

import com.amazonaws.auth.SystemPropertiesCredentialsProvider
import com.amazonaws.regions.Region
import com.amazonaws.regions.Regions
import com.amazonaws.regions.Regions.AP_SOUTHEAST_1
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.iterable.S3Objects
import com.amazonaws.services.s3.transfer.TransferManager
import eventstore.tools.internal.fileName
import util.cpu
import util.global.*
import util.misc.RefCountRepeatingTask
import java.io.File
import java.util.concurrent.TimeUnit.MINUTES

/**
 * Tracks the stream for rollups and uploads them to the AWS S3 storage, credentials must be available via system properties...
 */
class ESUploader(
        val source: String,
        val bucket: String = "jarvis-historical",
        val folder: String,
        val region: Regions = AP_SOUTHEAST_1) {
    private val log = logger("StreamUploader")

    val s3: AmazonS3Client

    val task = RefCountRepeatingTask(
            name = "stream-uploader",
            task = {
                // no failures are accepted
                executeMandatory { this.check() }
            },
            delay = 5, unit = MINUTES
    )

    init {
        // validate the source
        val root = File(source)
        condition(root.exists(), "folder $source does not exist")
        condition(root.isDirectory, "$source is not a directory")

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
        log.debug { "${task.name} : checking for pending uploads" }
        val root = File(source)

        if (!root.exists() || !root.isDirectory) return

        // collect data about local files
        val localFiles = root.listFiles()
        val lastModified = root.listFiles().map { it.lastModified() }.max() ?: return

        // collect data about remote files
        val remoteFiles = S3Objects.inBucket(s3, bucket).map { it.fileName() }.toList()

        val tm = TransferManager(s3, cpu.executors.io)

        // upload all local files that are not uploaded except the currently used one
        localFiles
                .filter { it.lastModified() < lastModified }
                .filter { remoteFiles.notContains(it.name) }
                .forEach { upload(it, tm) }

        tm.shutdownNow(false)
    }

    private fun upload(file: File, tm: TransferManager) {
        val destination = "$folder/${file.name}"

        log.info { "initiating upload from ${file.name} to $bucket/$destination" }

        val upload = tm.upload(bucket, destination, file)

        sleepLoopUntil(
                condition = { upload.isDone },
                block = {
                    log.info { "${upload.description} (${upload.state})"}
                    log.debug { "progress : ${upload.progress.percentTransferred.roundDown2()} %  (${size(upload.progress.bytesTransferred)})" }
                },
                delay = 5000
        )

        log.info { "successfully uploaded : ${file.name}" }

        file.delete()

        log.info { "removed local ${file.name}" }
    }


}
