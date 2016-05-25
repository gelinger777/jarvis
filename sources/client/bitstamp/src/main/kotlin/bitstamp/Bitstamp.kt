package bitstamp

import com.google.gson.JsonParser
import com.pusher.client.Pusher
import common.IAccount
import common.IExchange
import common.IMarket
import common.global.pair
import proto.bitfinex.ProtoBitstamp
import proto.common.Pair
import proto.common.Trade
import rx.subjects.PublishSubject


class Bitstamp(val config: ProtoBitstamp.BitstampConfig) : IExchange {
    override fun name(): String {
        throw UnsupportedOperationException()
    }

    override fun market(pair: Pair): IMarket {
        throw UnsupportedOperationException()
    }

    override fun account(): IAccount {
        throw UnsupportedOperationException()
    }

    override fun pairs(): List<Pair> {
        return listOf(pair("BTC", "USD"), pair("BTC", "EUR"))
    }

//    override fun streamTrades(pair: Pair): Observable<Trade> {
//        if (pair == pair("BTC", "USD")) {
//            util.net.pusher.stream(config.pusherKey, "live_trades", "trade")
//            .map { it.asTrade() }
//        }else{
//
//        }
//
//        return notImplemented()
//    }
//
//    override fun streamOrders(pair: Pair): Observable<Order> {
//        return notImplemented()
//    }



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