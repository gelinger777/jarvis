package common

import proto.common.Currency
import proto.common.Order
import proto.common.Pair
import proto.common.Trade
import rx.Observable
import util.app

interface IExchange {

    fun name(): String

    fun pairs(): List<Pair>

    fun market(pair: Pair): IMarket

    fun account(): IAccount

}

interface IMarket {

    fun exchange(): IExchange

    fun pair(): Pair

    fun orderbook(): IOrderBook

    fun trades(): Observable<Trade>

    fun orders(): Observable<Order>

}

/**
 * Immutable representation of an orderbook.
 */
data class Orderbook(
        val bids: List<Order> = emptyList(),
        val asks: List<Order> = emptyList(),
        val time: Long = app.time()
)

interface IOrderBook {

    fun snapshot(): Orderbook

    fun stream(): Observable<Order>

}

interface IAccount {

    fun exchange(): IExchange

    fun balances(): Map<Currency, Double>

}