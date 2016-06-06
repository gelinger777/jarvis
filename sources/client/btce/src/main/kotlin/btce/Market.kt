package btce

import btce.internal.pollOrders
import common.DiffAggregatedOrderbook
import common.IExchange
import common.IMarket
import common.IOrderBook
import common.global.all
import common.global.compact
import proto.common.Order
import proto.common.Pair
import proto.common.Trade
import rx.Observable
import util.global.condition
import util.global.logger
import util.misc.RefCountSchTask

internal class Market(val exchange: Btce, val pair: Pair) : IMarket {

    val log by logger("btce")

    val book = DiffAggregatedOrderbook()
    val orderFetcher = RefCountSchTask(
            name = "orderbook-poller:${exchange.name()}|${pair.compact()}",
            task = {
                log.debug("--------------------------------------------")
                pollOrders(pair).ifPresent {
                    val all = it.all()
                    book.accept(it)

                    log.debug("verifying")

                    condition(book.snapshot().all().size == all.size, "orderbooks must perfectly match")
                }

            },
            delay = 420
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

    override fun orders(): Observable<Order> {
        return book.stream()
    }

    override fun trades(): Observable<Trade> {
        throw UnsupportedOperationException()
    }
}