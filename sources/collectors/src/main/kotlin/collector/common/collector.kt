package collector.common

import collector.common.internal.ordersRelativePath
import collector.common.internal.tradesRelativePath
import common.IExchange
import common.IMarket
import common.global.asPair
import common.global.compact
import common.global.encodeOrders
import common.global.encodeTrades
import eventstore.tools.io.bytes.BytesWriter
import eventstore.tools.net.QueueUploader
import net.openhft.chronicle.queue.RollCycles
import net.openhft.chronicle.queue.RollCycles.MINUTELY
import util.app
import util.app.log
import util.global.*
import util.heartBeat

fun startCollectorFor(client: IExchange) {
    app.ensurePropertiesAreProvided(
            "store.path", // root path where event streams are being recorded
            "aws.accessKeyId", // aws credentials for s3 client
            "aws.secretKey", // aws credentials for s3 client
            "aws.bucket", // aws bucket where recorded data will be uploaded
            "record.trades", // trade streams to record
            "record.orders" // order streams to record
    )

    // get the pairs
    val tradePairs = app.property("record.trades").split(",")
            .map { executeAndGetMandatory { it.asPair() } }

    val orderPairs = app.property("record.orders").split(",")
            .map { executeAndGetMandatory { it.asPair() } }

    // get the rollup cycles
    val tradeCycles = app.optionalProperty("trade.cycles")
            .flatMap { executeAndGetSilent { RollCycles.valueOf(it) } }
            .ifNotPresentTake(MINUTELY).get()

    val orderCycles = app.optionalProperty("order.cycles")
            .flatMap { executeAndGetSilent { RollCycles.valueOf(it) } }
            .ifNotPresentTake(MINUTELY).get()

    // start collecting trades
    for (pair in tradePairs) {
        log.info { "collecting trades of ${client.name()}|${pair.compact()}" }

        collectTrades(
                market = client.market(pair),
                relativePath = tradesRelativePath(client, pair),
                cycles = tradeCycles
        )
    }

    // start collecting orders
    for (pair in orderPairs) {
        log.info { "collecting orders of ${client.name()}|${pair.compact()}" }

        collectOrders(
                market = client.market(pair),
                relativePath = ordersRelativePath(client, pair),
                cycles = orderCycles
        )
    }
}

private fun collectTrades(market: IMarket, relativePath: String, cycles: RollCycles) {
    val absolutePath = "${app.property("store.path")}/$relativePath"

    val writer = BytesWriter(path = absolutePath, cycles = cycles)

    market.trades()
            .encodeTrades()
            .forEach {
                writer.write(it)
                heartBeat.beat(relativePath)
            }

    QueueUploader(
            localPath = absolutePath,
            remotePath = relativePath,
            bucket = app.property("aws.bucket"),
            delay = 5.minutes()
    )


    heartBeat.start(
            name = relativePath,
            timeout = 1.hours(),
            callback = { report("no events for a while") },
            keepAlive = true
    )
}

private fun collectOrders(market: IMarket, relativePath: String, cycles: RollCycles) {
    val absolutePath = "${app.property("store.path")}/$relativePath"

    val writer = BytesWriter(path = absolutePath, cycles = cycles)

    market.orders()
            .encodeOrders()
            .forEach {
                writer.write(it)
                heartBeat.beat(relativePath)
            }

    QueueUploader(
            localPath = absolutePath,
            remotePath = relativePath,
            bucket = app.property("aws.bucket"),
            delay = 5.minutes()
    )

    heartBeat.start(
            name = relativePath,
            timeout = 1.hours(),
            callback = { report("no events for a while") },
            keepAlive = true
    )
}