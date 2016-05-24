package common

import proto.common.Order
import rx.Observable

class OrderBook : IOrderBook {

    override fun market(): IMarket {
        throw UnsupportedOperationException()
    }

    override fun bids(): List<Order> {
        throw UnsupportedOperationException()
    }

    override fun asks(): List<Order> {
        throw UnsupportedOperationException()
    }

    override fun stream(): Observable<Order> {
        throw UnsupportedOperationException()
    }
}