package collector

import client.bitstamp.Bitstamp
import client.btce.Btce
import common.IMarket
import common.global.pair
import util.global.hour
import util.global.sleepLoop
import util.heartBeat
import util.net

internal fun main(args: Array<String>) {

    with(pair("btc", "usd")){
//        with(Bitfinex().market(this)){
//            trackOrders(this)
//            trackTrades(this)
//        }
        with(Bitstamp().market(this)){
            trackOrders(this)
            trackTrades(this)
        }
        with(Btce().market(this)){
            trackOrders(this)
            trackTrades(this)
        }
    }

    sleepLoop({ false }, {
        heartBeat.status()
    })
}

fun trackTrades(market: IMarket) {
    val name = "${market.name()}|trades"

    market.trades().forEach { heartBeat.beat(name) }

    heartBeat.add(name, 1.hour(), {
        net.mail.send(
                subject = "$name is not responding",
                message = "no events for more than 1 hour",
                destination = "vachagan.balayan@gmail.com",
                senderName = "Jarvis",
                senderAddress = "jarvis@jarvis.com"
        )
    })
}

fun trackOrders(market: IMarket) {
    val name = "${market.name()}|orders"

    market.orders().forEach { heartBeat.beat(name) }

    heartBeat.add(name, 1.hour(), {
        net.mail.send(
                subject = "$name is not responding",
                message = "no events for more than 1 hour",
                destination = "vachagan.balayan@gmail.com",
                senderName = "Jarvis",
                senderAddress = "jarvis@jarvis.com"
        )
    })
}

