package eventstore.tools

import com.amazonaws.auth.SystemPropertiesCredentialsProvider
import com.amazonaws.regions.Region
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3Client
import util.app
import util.global.condition
import util.global.executeAndGetMandatory
import util.global.executeMandatory
import util.global.logger
import util.misc.RefCountRepeatingTask

/**
 * Polls data from remote data storage.
 */
class StreamPoller(
        val destination: String,
        val bucket: String = "jarvis-historical",
        val folder: String,
        val region: Regions = Regions.AP_SOUTHEAST_1) {
    private val log = logger("StreamPoller")

    var lastCheck = -1L;

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
        log.debug { "${task.name} : checking for new data" }

        val listObjects = s3.listObjects(bucket)

        app.log.info(listObjects)

//        val root = File(source)
//
//        if (!root.exists() || !root.isDirectory) return
//
//        val files = root.listFiles()
//
//        val lastModified = root.listFiles().map { it.lastModified() }.max() ?: return
//
//        files.filter { it.lastModified() < lastModified }.forEach { upload(it) }
//
//        lastCheck = app.time()
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