package eventstore.tools

import com.amazonaws.auth.SystemPropertiesCredentialsProvider
import com.amazonaws.regions.Region
import com.amazonaws.regions.Regions
import com.amazonaws.regions.Regions.AP_SOUTHEAST_1
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.PutObjectRequest
import util.app
import util.global.condition
import util.global.executeAndGetMandatory
import util.global.executeMandatory
import util.global.logger
import util.misc.RefCountSchTask
import java.io.File

/**
 * Tracks the stream for rollups and uploads them to the AWS S3 storage, credentials must be available via system properties...
 */
class StreamUploader(
        val source: String,
        val bucket: String = "jarvis-historical",
        val folder: String,
        val region: Regions = AP_SOUTHEAST_1) {
    private val log = logger("StreamUploader")

    var lastCheck = -1L;

    val s3: AmazonS3Client

    val task = RefCountSchTask(
            name = "stream-uploader : $bucket/$folder",
            task = {
                // no failures are accepted
                executeMandatory { this.check() }
            },
            delay = 10000 // todo change to 5 minutes
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
        val remoteFiles = s3.listObjects(bucket).objectSummaries.map { it.key.removePrefix("$folder/") }

        // upload all local files that are not uploaded except the currently used one
        localFiles
                .filter { it.lastModified() < lastModified }
                .filter { remoteFiles.notContains(it.name) }
                .forEach { upload(it) }

        lastCheck = app.time()
    }

    private fun upload(file: File) {
        val destination = "$folder/${file.name}"

        log.info { "uploading ${file.name} to $bucket/$destination" }

        s3.putObject(PutObjectRequest(bucket, destination, file))

        log.info { "successfully uploaded : ${file.name}" }

        file.delete()

        log.info { "removed local ${file.name}" }
    }
}

fun <E> Collection<E>.notContains(element: E): Boolean {
    return !contains(element)
}
