package collector.common

import collector.common.internal.ordersRelativePath
import collector.common.internal.tradesRelativePath
import common.IExchange
import common.IMarket
import common.global.asPair
import common.global.encodeOrders
import common.global.encodeTrades
import eventstore.tools.io.bytes.BytesWriter
import eventstore.tools.net.s3Uploader
import net.openhft.chronicle.queue.RollCycles
import net.openhft.chronicle.queue.RollCycles.HOURLY
import net.openhft.chronicle.queue.RollCycles.MINUTELY
import proto.common.Pair
import util.app
import util.app.log
import util.global.executeAndGetMandatory
import util.global.executeAndGetSilent
import util.global.minutes
import util.global.report
import util.heartBeat

fun startCollectorFor(client: IExchange) {
    app.ensurePropertiesAreProvided(
            "record.trades", // trade streams to record
            "record.orders", // order streams to record

            "trade.cycles",
            "order.cycles",

            // maximum timeout of silence before alerts are created
            "trade.stream.maxTimeout",
            "order.stream.maxTimeout",

            "store.path", // root path where event streams are being recorded

            "aws.accessKeyId", // aws credentials for s3 client
            "aws.secretKey", // aws credentials for s3 client
            "aws.bucket", // aws bucket where recorded data will be uploaded
            "aws.region",

            "version"
    )

    // start collecting trades
    for (pair in tradePairs()) {
        val market = client.market(pair)

        log.info { "collecting trades of ${market.name()}" }

        collectTrades(
                market = market,
                relativePath = tradesRelativePath(client, pair)
        )
    }

    // start collecting orders
    for (pair in orderPairs()) {
        val market = client.market(pair)

        log.info { "collecting orders of ${market.name()}" }

        collectOrders(
                market = market,
                relativePath = ordersRelativePath(client, pair)
        )
    }
}

private fun collectTrades(market: IMarket, relativePath: String) {
    val absolutePath = "${storePath()}/$relativePath"

    val writer = BytesWriter(path = absolutePath, cycles = tradeCycle())

    market.trades()
            .encodeTrades()
            .forEach {
                writer.write(it)
                heartBeat.beat(relativePath)
            }

    s3Uploader.add(
            localPath = absolutePath,
            remotePath = "${app.property("version")}/$relativePath"
    )

    heartBeat.add(
            name = relativePath,
            timeout = tradeTimeout(),
            callback = { report("no events for a while") },
            keepAlive = true
    )
}

private fun collectOrders(market: IMarket, relativePath: String) {
    val absolutePath = "${storePath()}/$relativePath"

    val writer = BytesWriter(path = absolutePath, cycles = orderCycle())

    market.orders()
            .encodeOrders()
            .forEach {
                writer.write(it)
                heartBeat.beat(relativePath)
            }

    s3Uploader.add(
            localPath = absolutePath,
            remotePath = "${app.property("version")}/$relativePath"
    )

    heartBeat.add(
            name = relativePath,
            timeout = orderTimeout(),
            callback = { report("no events for a while") },
            keepAlive = true
    )
}

fun storePath(): String {
    return app.property("store.path")
}

fun tradePairs(): List<Pair> {
    return app.property("record.trades").split(",").map { executeAndGetMandatory { it.asPair() } }
}

fun orderPairs(): List<Pair> {
    return app.property("record.orders").split(",").map { executeAndGetMandatory { it.asPair() } }
}

fun tradeCycle(): RollCycles {
    return app.optionalProperty("trade.cycles").flatMap { executeAndGetSilent { RollCycles.valueOf(it) } }
            .ifNotPresentTake(HOURLY).get()
}

fun orderCycle(): RollCycles {
    return app.optionalProperty("order.cycles").flatMap { executeAndGetSilent { RollCycles.valueOf(it) } }
            .ifNotPresentTake(MINUTELY).get()
}

fun tradeTimeout(): Long {
    return app.property("trade.stream.maxTimeout").toInt().minutes()
}

fun orderTimeout(): Long {
    return app.property("order.stream.maxTimeout").toInt().minutes()
}
