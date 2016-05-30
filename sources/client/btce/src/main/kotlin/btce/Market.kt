package btce

import btce.internal.pollOrders
import common.DiffAggregatedOrderbook
import common.IExchange
import common.IMarket
import common.IOrderBook
import common.global.all
import common.global.asKey
import proto.common.Order
import proto.common.Pair
import proto.common.Trade
import rx.Observable
import util.global.condition
import util.misc.RefCountSchTask

internal class Market(val exchange: Btce, val pair: Pair) : IMarket {

    val book = DiffAggregatedOrderbook()
    val orderFetcher = RefCountSchTask(
            name = "orderbook-poller:${exchange.name()}|${pair.asKey()}",
            task = {
                println("--------------------------------------------")
                pollOrders(pair).ifPresent {
                    val all = it.all()
                    book.accept(it)

                    println("verifying")

                    condition(book.snapshot().all().size == all.size)
                }

            },
            delay = 500
    )

    init {

        // start polling task for orderbook
        orderFetcher.forceStart()


        // start polling task for trades

    }

    override fun exchange(): IExchange {
        return exchange
    }

    override fun pair(): Pair {
        return pair
    }

    override fun orderbook(): IOrderBook {
        return book
    }

    override fun trades(): Observable<Trade> {
        throw UnsupportedOperationException()
    }

    override fun orders(): Observable<Order> {
        return book.stream()
    }
}