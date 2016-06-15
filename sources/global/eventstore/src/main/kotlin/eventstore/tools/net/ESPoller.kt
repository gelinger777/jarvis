package eventstore.tools.net

import com.amazonaws.auth.SystemPropertiesCredentialsProvider
import com.amazonaws.regions.Region
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.iterable.S3Objects
import com.amazonaws.services.s3.model.S3ObjectSummary
import com.amazonaws.services.s3.transfer.TransferManager
import eventstore.tools.internal.fileName
import util.cpu
import util.global.*
import util.misc.RefCountRepeatingTask
import java.io.File

/**
 * Polls data from remote data storage.
 */
class ESPoller(
        val destination: String,
        val bucket: String = "jarvis-historical",
        val folder: String,
        val region: Regions = Regions.AP_SOUTHEAST_1) {
    private val log = logger("StreamPoller")

    val s3: AmazonS3Client

    val task = RefCountRepeatingTask(
            name = "stream-poller : $bucket/$folder",
            task = {
                // no failures are accepted
                executeMandatory { this.check() }
            },
            delay = 10000
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
        val root = File(destination)

        if (!root.exists() || !root.isDirectory) return

        // collect data about local files
        val localFiles = root.listFiles().map { it.name }

        // collect data about remote files
        val remoteFiles = S3Objects.inBucket(s3, bucket).toList()

        // download all remote files not found in local file system
        remoteFiles
            .filter { localFiles.notContains(it.fileName()) }
            .forEach { download(it) }
    }

    private fun download(s3object: S3ObjectSummary) {


        val tm = TransferManager(s3, cpu.executors.io)

//        s3.getObject(GetObjectRequest())
//
//        return notImplemented()
    }

//    private fun upload(file: File) {
//        val destination = "$folder/${file.name}"
//
//        log.info { "uploading ${file.name} to $bucket/$destination" }
//
//        s3.putObject(PutObjectRequest(bucket, destination, file))
//
//        log.info { "successfully uploaded : ${file.name}" }
//
//        file.delete()
//
//        log.info { "removed local ${file.name}" }
//    }
}