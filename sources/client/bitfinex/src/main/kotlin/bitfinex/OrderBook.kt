package bitfinex

import common.IMarket
import common.IOrderBook
import proto.bitfinex.ProtoBitfinex
import proto.common.Order
import rx.Observable
import rx.subjects.PublishSubject
import util.global.roundDown5
import java.util.concurrent.ThreadLocalRandom

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

fun main(args: Array<String>) {

//45.36469
    val rand = ThreadLocalRandom.current()

//    val map = LinkedHashMap<Int, String>().toSortedMap(Comparator { first, second -> first.compareTo(second) })
//
//    for (i in 1..10) {
//        val random = rand.nextInt(0, 10)
//
//        println(random)
//
//        map.put(random, random.toString())
//
//        map.forEach { println("<> : "+it.key) }
//
//    }

    var num = 0.0

    for (i in 0..100) {
        val rounded = roundDown5(rand.nextDouble())
        val next = num + rounded
        println("$num + $rounded = $next")
        num = next
    }


}