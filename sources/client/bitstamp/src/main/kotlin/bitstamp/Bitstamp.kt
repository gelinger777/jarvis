package bitstamp

import com.google.gson.JsonParser
import com.pusher.client.Pusher
import common.IExchange
import common.util.pair
import proto.bitfinex.BitstampConfig
import proto.common.Order
import proto.common.Pair
import proto.common.Trade
import rx.Observable
import rx.subjects.PublishSubject
import util.global.notImplemented


class Bitstamp(val config: BitstampConfig) : IExchange {

    override fun pairs(): List<Pair> {
        return listOf(pair("BTC", "USD"), pair("BTC", "EUR"))
    }

    override fun streamTrades(pair: Pair): Observable<Trade> {
        if (pair == pair("BTC", "USD")) {
            util.net.pusher.stream(config.pusherKey, "live_trades", "trade")
            .map { it.asTrade() }
        }else{

        }

        return notImplemented()
    }

    override fun streamOrders(pair: Pair): Observable<Order> {
        return notImplemented()
    }



    private fun String.asTrade(): Trade {
        val rootElement = JsonParser().parse(this)

        return Trade.getDefaultInstance()
    }

    private fun String.asOrderbook(){}
}

fun main(args: Array<String>) {
    val pusher = Pusher("de504dc5763aeef9ff52")
    pusher.connect()

    val channel = pusher.subscribe("diff_order_book")
    val subject = PublishSubject.create<String>()

    subject.subscribe { println(it) }

    channel.bind("data") { ch, ev, data ->
        subject.onNext(data)
    }

    util.net.pusher.stream("de504dc5763aeef9ff52", "order_book", "data")
            .subscribe { println(it) }

    readLine()
}