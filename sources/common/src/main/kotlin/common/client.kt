package common

import proto.common.Currency
import proto.common.Order
import proto.common.Pair
import proto.common.Trade
import rx.Observable

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

    fun streamTrades(): Observable<Trade>

    fun streamOrders(): Observable<Order>

}

interface IOrderBook {

    fun market(): IMarket

    /**
     * Bid orders sorted by descending order.
     */
    fun bids(): List<Order>

    /**
     * Ask orders sorted by ascending order.
     */
    fun asks(): List<Order>

    /**
     * Observable stream of order events.
     */
    fun stream(): Observable<Order>

}

interface IAccount {

    fun exchange(): IExchange

    fun balances(): IBalance

}

interface IBalance {

    fun account(): IAccount

    fun currency(): Currency

    fun amount(): Double

}
