package common

import proto.common.*
import rx.Observable

/**
 * Common interface for all collector services.
 *
 * Clients are implementing this interface, Servers implement CollectorGrpc.Collector interface.
 */
interface IExchangeCollector {

    fun status(request: CollStatusReq): CollStatusResp

    fun streamTrades(request: StreamTradesReq): Observable<Trade>

    fun streamOrders(request: StreamOrdersReq) : Observable<Order>

    fun recordTrades(request: RecordTradesReq): RecordTradesResp

    fun recordOrders(request: RecordOrdersReq): RecordOrdersResp

    fun streamHistoricalTrades(request: StreamHistoricalTradesReq):Observable<Trade>

    fun streamHistoricalOrders(request: StreamHistoricalOrdersReq) : Observable<Order>
}