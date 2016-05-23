package common

import proto.common.Order
import proto.common.Pair
import proto.common.Trade
import rx.Observable

interface IExchangeClient {

    fun symbols(): List<Pair>

    fun streamTrades(pair: Pair): Observable<Trade>

    fun streamOrders(pair: Pair): Observable<Order>

}