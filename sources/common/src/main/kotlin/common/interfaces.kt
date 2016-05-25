package common

import proto.common.*
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

    fun trades(): Observable<Trade>

    fun orders(): Observable<Order>

}

interface IOrderBook {

    fun accept(order : Order)

    fun bids(): List<Order>

    fun asks(): List<Order>

}

interface IAccount {

    fun exchange(): IExchange

    fun balances(): Map<Currency, Double>

}

/**
 * Common interface for all collector services.
 *
 * Clients are implementing this interface, Servers implement CollectorGrpc.Collector interface.
 */
interface ICollector {

    fun info(request: CollInfoReq): CollInfoResp

    fun streamTrades(request: StreamTradesReq): Observable<Trade>

    fun streamOrders(request: StreamOrdersReq) : Observable<Order>

    fun recordTrades(request: RecordTradesReq): RecordTradesResp

    fun recordOrders(request: RecordOrdersReq): RecordOrdersResp

    fun streamHistoricalTrades(request: StreamHistoricalTradesReq): Observable<Trade>

    fun streamHistoricalOrders(request: StreamHistoricalOrdersReq) : Observable<Order>
}