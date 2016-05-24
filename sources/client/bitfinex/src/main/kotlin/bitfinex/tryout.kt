package bitfinex

import common.util.bitfinexConfig
import common.util.json
import common.util.pair
import util.app

fun main(args: Array<String>) {
    val bitfinex = Bitfinex(
            bitfinexConfig(
                    wsUrl = "wss://api2.bitfinex.com:3000/ws",
                    publicKey = "8SOAdEL7gPLgB0zz7KiqdOqmIHMw5vfgRnVlFbytfKa",
                    privateKey = "a9pZv0zDvprfK0PvUF7wKZGzeU16m06ZvM7CHQRfncY"
            )
    )

//    bitfinex.pairs().forEach { println(it.asKey()) }


    val market = bitfinex.market(pair("btc", "usd"))

    market.streamTrades().subscribe { app.log.info(it.json()) }
    market.streamOrders().subscribe { app.log.info(it.json()) }

    readLine()
}