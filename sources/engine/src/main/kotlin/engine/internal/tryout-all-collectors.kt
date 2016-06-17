package engine.internal

import client.bitfinex.Bitfinex
import common.IMarket
import common.global.pair
import util.global.sleepLoopUntil
import util.heartBeat
import util.net
import java.util.concurrent.TimeUnit

internal fun main(args: Array<String>) {

    with(pair("btc", "usd")){
        with(Bitfinex().market(this)){
            trackOrders(this)
            trackTrades(this)
        }
//        with(Bitstamp().market(this)){
//            trackOrders(this)
//            trackTrades(this)
//        }
//        with(Btce().market(this)){
//            trackOrders(this)
//            trackTrades(this)
//        }
    }

    sleepLoopUntil({ false }, {
        heartBeat.status()
    })
}

fun trackTrades(market: IMarket) {
    val name = "${market.name()}|trades"

    market.trades().forEach { heartBeat.beat(name) }

    heartBeat.start(name, TimeUnit.MINUTES.toMillis(10), {
        net.mail.send(
                subject = "$name is not responding",
                message = "no events for more than 10 minutes",
                destination = "vachagan.balayan@gmail.com",
                senderName = "Jarvis",
                senderAddress = "jarvis@jarvis.com"
        )
    })
}

fun trackOrders(market: IMarket) {
    val name = "${market.name()}|orders"

    market.orders().forEach { heartBeat.beat(name) }

    heartBeat.start(name, TimeUnit.MINUTES.toMillis(10), {
        net.mail.send(
                subject = "$name is not responding",
                message = "no events for more than 10 minutes",
                destination = "vachagan.balayan@gmail.com",
                senderName = "Jarvis",
                senderAddress = "jarvis@jarvis.com"
        )
    })
}

