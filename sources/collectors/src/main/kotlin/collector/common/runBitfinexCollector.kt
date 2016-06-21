package collector.common

import client.bitfinex.Bitfinex
import util.app
import util.global.sleepLoop
import util.heartBeat

fun main(args: Array<String>) {
    app.ensurePropertiesAreProvided(
            "store.path", // root path where event streams are being recorded
            "aws.accessKeyId", // aws credentials for s3 client
            "aws.secretKey", // aws credentials for s3 client
            "aws.bucket", // aws bucket where recorded data will be uploaded
            "record.trades", // trade streams to record
            "record.orders" // order streams to record
    )

    app.log.info { "starting Bitfinex collector" }

    startCollectorFor(Bitfinex())

    sleepLoop(task = { heartBeat.status() })
}