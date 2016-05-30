package common.internal

import common.DiffAggregatedOrderbook
import common.Orderbook
import common.global.json
import common.global.order
import proto.common.Order.Side.ASK
import proto.common.Order.Side.BID

internal fun main(args: Array<String>) {
    val book = DiffAggregatedOrderbook()

    book.stream().subscribe { println(it.json()) }

    book.accept(
            Orderbook(
                    time = 1,
                    bids = listOf(
                            order(BID, 1.0, 0.5, 1),
                            order(BID, 2.0, 0.5, 1)
                    ),
                    asks = listOf(
                            order(ASK, 3.0, 0.5, 1),
                            order(ASK, 4.0, 0.5, 1)
                    )
            )
    )

    println()
    readLine()

    book.accept(
            Orderbook(
                    time = 2,
                    bids = listOf(
                            order(BID, 1.1, 0.5, 1),
                            order(BID, 2.0, 1.5, 1)
                    ),
                    asks = listOf(
                            order(ASK, 3.0, 0.5, 1),
                            order(ASK, 4.0, 0.5, 1)
                    )
            )
    )

}