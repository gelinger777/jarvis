package internal

import proto.common.Order
import rx.Observable
import util.global.notImplemented


class OrderStreamSynchronizer {

    fun feedSnapshot(snapshot: OrderBookSnapshot) {

    }

    fun feedOrder(order: Order) {

    }

    fun stream(): Observable<Order> {
        return notImplemented()
    }

}

data class OrderBookSnapshot(val time: Long, val orders: List<Order>)