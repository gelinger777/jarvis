package client.btce

import client.btce.internal.filterNewTrades
import client.btce.internal.pollOrders
import client.btce.internal.pollTrades
import common.AggregatedOrderbook
import common.IExchange
import common.IMarket
import common.IOrderBook
import common.global.compact
import proto.common.Order
import proto.common.Pair
import proto.common.Trade
import rx.Observable
import util.Option
import util.global.filterEmptyOptionals
import util.global.logger
import util.global.seconds
import util.global.unpack
import util.misc.RefCountRepeatingProducer
import util.misc.RefCountRepeatingTask

internal class Market(val exchange: Btce, val pair: Pair) : IMarket {
    internal val log = logger("${exchange.name}|${pair.compact()}")

    val tradePollTask = RefCountRepeatingProducer(
            name = "trades-producer:${exchange.name()}|${pair.compact()}",
            producer = {
                log.debug("polling trades")
                pollTrades(pair)
            },
            delay = 2.seconds()
    )

    val trades = tradePollTask.stream().filterEmptyOptionals()
            .map { filterPreviouslyObservedTrades(it) }
            .filterEmptyOptionals()
            .unpack()


    val book = AggregatedOrderbook()

    val orderPollTask = RefCountRepeatingTask(
            name = "orders-producer:${exchange.name()}|${pair.compact()}",
            task = {
                log.debug("polling orders")
                pollOrders(pair).ifPresent { book.accept(it) }
            },
            delay = 2.seconds()
    )

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
        log.info { "streaming orders" }
        orderPollTask.increment()
        return book.stream()
    }

    override fun trades(): Observable<Trade> {
        log.info { "streaming trades" }
        tradePollTask.increment()
        return trades
    }

    // stuff

    private var previousTradesBatch = emptyList<Trade>()

    private fun filterPreviouslyObservedTrades(batch: List<Trade>): Option<List<Trade>> {
        val result = previousTradesBatch.filterNewTrades(batch)
        previousTradesBatch = batch
        return result
    }

}


