package bitfinex

import common.IMarket
import common.IOrderBook
import proto.bitfinex.ProtoBitfinex
import proto.common.Order
import rx.Observable
import rx.subjects.PublishSubject

internal class OrderBook(val market: Market) : IOrderBook {
    val orders = PublishSubject.create<Order>()


    override fun market(): IMarket {
        return market
    }

    override fun asks(): List<Order> {
        throw UnsupportedOperationException()
    }

    override fun bids(): List<Order> {
        throw UnsupportedOperationException()
    }

    override fun stream(): Observable<Order> {
        return orders
    }

    // stuff

    fun acceptNext(order: ProtoBitfinex.Order) {
        orders.onNext(
                Order.newBuilder()
                        .setTime(order.time)
                        .setPrice(order.price)
                        .setVolume(order.volume)
                        .build()
        )
    }

}