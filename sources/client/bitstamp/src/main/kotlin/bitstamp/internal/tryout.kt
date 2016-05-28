package bitstamp.internal

import common.AggregatedOrderbook
import common.OrderStreamSync
import common.global.json
import common.global.pair

fun main(args: Array<String>) {

    val pair = pair("btc", "usd")

    val sync = OrderStreamSync(
            fetcher = { getOrderbookSnapshot(pair) },
            delay = 3000
    );
    util.net.pusher.stream("de504dc5763aeef9ff52", "diff_order_book", "data")
            .map { parseOrdersFromDiff(it) }
            .subscribe { it.orders.forEach { sync.next(it) } }

    val book = AggregatedOrderbook()
    sync.stream.subscribe { book.accept(it) }
    sync.stream.subscribe { println(it.json()) }


    readLine()
}