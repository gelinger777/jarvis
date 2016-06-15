package eventstore.tools.net

import com.amazonaws.auth.SystemPropertiesCredentialsProvider
import com.amazonaws.regions.Region
import com.amazonaws.regions.Regions
import com.amazonaws.regions.Regions.AP_SOUTHEAST_1
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.iterable.S3Objects
import com.amazonaws.services.s3.transfer.TransferManager
import eventstore.tools.internal.fileName
import eventstore.tools.internal.isChronicleFile
import util.global.*
import util.misc.RefCountRepeatingTask
import java.io.File
import java.util.concurrent.TimeUnit.MINUTES

/**
 * Tracks the stream for rollups and uploads them to the AWS S3 storage, credentials must be available via system properties...
 */
class ESUploader(
        val localRoot: String,
        val bucket: String = "jarvis-historical",
        val folder: String,
        val region: Regions = AP_SOUTHEAST_1) {
    private val log = logger("ESUploader")

    val s3: AmazonS3Client

    val task = RefCountRepeatingTask(
            name = "es-uploader",
            task = {
                // no failures are accepted
                executeMandatory { this.check() }
            },
//            delay = 2000
            delay = 5, unit = MINUTES
    )

    init {
        // validate the source
        val root = File(localRoot)
        condition(root.exists(), "folder $localRoot does not exist")
        condition(root.isDirectory, "$localRoot is not a directory")

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
        val localFiles = localRoot.listFiles()
        val lastModified = localRoot.listFiles().map { it.lastModified() }.max() ?: return

        // collect data about remote files
        val remoteFiles = S3Objects.inBucket(s3, bucket)
                .filter { it.isChronicleFile() }.map { it.fileName() }.toList()

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
        val destination = "$folder/${file.name}"

        log.info { "initiating upload from ${file.path} to s3:$bucket/$destination" }

        val upload = tm.upload(bucket, destination, file)

        sleepLoopUntil(
                condition = { upload.isDone },
                block = {
                    log.info { "${upload.description} (${upload.state})" }
                    log.debug { "progress : ${upload.progress.percentTransferred.roundDown2()} %  (${size(upload.progress.bytesTransferred)})" }
                },
                delay = 1000
        )

        log.info { "successfully uploaded : ${file.name}" }

        file.delete()

        log.info { "removed local ${file.name}" }
    }

}
