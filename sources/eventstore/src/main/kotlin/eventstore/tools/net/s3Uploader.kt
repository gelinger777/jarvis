package eventstore.tools.net

import com.amazonaws.auth.SystemPropertiesCredentialsProvider
import com.amazonaws.regions.Region
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.iterable.S3Objects
import com.amazonaws.services.s3.transfer.TransferManager
import eventstore.tools.internal.fileName
import util.app
import util.global.*
import util.misc.RefCountRepeatingTask
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors

object s3Uploader {
    private val log = logger("s3Uploader")
    private val registry = ConcurrentHashMap<String, String>()
    private val s3: AmazonS3Client
    private val bucket: String

    private val watchDogTask = RefCountRepeatingTask(
            name = "s3Uploader-watchdog",
            task = {
                // iterate over all monitored instances
                for ((from, to) in registry.entries) {
                    log.debug { "checking : ${from} for available uploads" }

                    check(from, to)
                }
            },
            delay = 3.second()
    )

    init {
        app.ensurePropertiesAreProvided(
                "aws.accessKeyId", // aws credentials for s3 client
                "aws.secretKey", // aws credentials for s3 client
                "aws.bucket", // aws bucket where recorded data will be uploaded
                "aws.region" // aws bucket where recorded data will be uploaded
        )

        bucket = app.property("aws.bucket")
        val region = Regions.fromName(app.property("aws.region"))

        s3 = AmazonS3Client(executeAndGetMandatory { SystemPropertiesCredentialsProvider().credentials })
        s3.setRegion(Region.getRegion(region))
        condition(s3.listBuckets().map { it.name }.contains(bucket), "bucket '$bucket' does not exist")
    }

    fun add(localPath: String, remotePath: String) {
        if (registry.containsKey(localPath)) {
            log.warn { "attempt to add existing upload at '$localPath'" }
        } else {
            registry.put(localPath, remotePath)
            watchDogTask.increment()
            log.info { "upload registered from '$localPath' to '$remotePath'" }
        }
    }

    fun remove(localPath: String) {
        if (registry.containsKey(localPath)) {
            registry.remove(localPath)
            watchDogTask.decrement()
            log.info { "removed upload for '$localPath'" }
        } else {
            log.warn { "attempt to remove unregistered upload '$localPath'" }
        }
    }

    fun status() {
        for((from, to) in registry.entries){
            log.info { "stream upload registered from '$from' to 's3:$bucket/$to" }
        }
    }

    fun check(from : String, to : String){
        val localRoot = File(from)

        if (!localRoot.exists() || !localRoot.isDirectory) {
            log.debug { "local source '$localRoot' is not valid skipping" }
            return
        }

        // collect data about local files
        val localFiles = localRoot.listFiles()
        val lastModified = localRoot.listFiles().map { it.lastModified() }.max() ?: return

        // collect data about remote files
        val remoteFiles = S3Objects.inBucket(s3, bucket)
                .filter { it.key.startsWith("$to/") && it.key.endsWith(".cq4") }.map { it.fileName() }.toList()

        val tm = TransferManager(s3, Executors.newFixedThreadPool(5))

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
                .forEach { upload(it, tm, to) }

        tm.shutdownNow(false)
    }

    private fun upload(file: File, tm: TransferManager, remotePath: String) {
        val destination = "$remotePath/${file.name}"

        log.info { "initiating upload from '${file.path}' to 's3:$bucket/$destination'" }

        val upload = tm.upload(bucket, destination, file)

        sleepLoop(
                condition = { upload.isDone },
                task = {
                    log.info { "${upload.description} (${upload.state})" }
                    log.debug { "progress : ${upload.progress.percentTransferred.roundDown2()} %  (${size(upload.progress.bytesTransferred)})" }
                },
                delay = 5.seconds()
        )

        log.info { "successfully uploaded : '${file.name}'" }

        file.delete()

        log.info { "removed local '${file.name}'" }
    }
}