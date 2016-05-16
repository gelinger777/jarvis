package bitfinex

import common.util.bitfinexConfig
import common.util.json
import common.util.pair
import indicator.ohlc.OHLC
import proto.common.OHLC
import util.app
import java.util.concurrent.TimeUnit.MINUTES

fun main(args: Array<String>) {

    val bitfinex = Bitfinex(
            bitfinexConfig(
                    websocketConnectionURL = "wss://api2.bitfinex.com:3000/ws",
                    publicKey = "8SOAdEL7gPLgB0zz7KiqdOqmIHMw5vfgRnVlFbytfKa",
                    privateKey = "a9pZv0zDvprfK0PvUF7wKZGzeU16m06ZvM7CHQRfncY"
            )
    )


    val data = mutableListOf<OHLC>()

    val stream = bitfinex.streamTrades(pair("BTC", "USD"))
            .OHLC(MINUTES.toMillis(1))
            .subscribe {
                data.add(it)
                app.log.info("${it.json()}")
            }

    readLine()

    println("all emitted OHLC events")

    data.forEach {
        println(it.json())
    }

    println("all completed OHLCs")

    data
            .filter { it.isCompleted }
            .forEach { println(it.json()) }

}
