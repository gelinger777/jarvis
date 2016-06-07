package btce

import btce.internal.diff
import btce.internal.filterNewTrades
import btce.internal.pollOrders
import btce.internal.pollTrades
import common.*
import common.global.compact
import proto.common.Order
import proto.common.Pair
import proto.common.Trade
import rx.Observable
import util.Option
import util.global.filterEmptyOptionals
import util.global.logger
import util.global.unpack
import util.misc.RefCountSchProducer

internal class Market(val exchange: Btce, val pair: Pair) : IMarket {

    val log by logger("btce")

    val tradePollTask = RefCountSchProducer(
            name = "trades-producer:${exchange.name()}|${pair.compact()}",
            producer = {
                pollTrades(pair)
            },
            delay = 420
    )

    val trades = tradePollTask.stream().filterEmptyOptionals()
            .map { filterPreviouslyObservedTrades(it) }
            .filterEmptyOptionals()
            .unpack()


    val book = AggregatedOrderbook()

    val orderPollTask = RefCountSchProducer(
            name = "orders-producer:${exchange.name()}|${pair.compact()}",
            producer = {
                log.debug("polling")
                pollOrders(pair)
            },
            delay = 420
    ).apply {
        this.stream().filterEmptyOptionals()
                .forEach { diffWithOrderbook(it) }
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
        orderPollTask.increment()
        return book.stream()
    }

    override fun trades(): Observable<Trade> {
        tradePollTask.increment()
        return trades
    }

    // stuff


    // trades

    private var previousTradesBatch = emptyList<Trade>()

    private fun filterPreviouslyObservedTrades(batch: List<Trade>): Option<List<Trade>> {
        val result = previousTradesBatch.filterNewTrades(batch)
        previousTradesBatch = batch
        return result
    }

    private fun diffWithOrderbook(newOrderbook: Orderbook) {
        // calculate diff
        book.bids.diff(newOrderbook.bids).forEach { book.accept(it) }
        book.asks.diff(newOrderbook.asks).forEach { book.accept(it) }
    }

}


