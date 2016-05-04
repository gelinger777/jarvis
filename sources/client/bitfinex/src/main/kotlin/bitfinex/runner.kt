package bitfinex

import common.util.bitfinexConfig
import common.util.json
import common.util.pair

fun main(args: Array<String>) {

    val bitfinex = Bitfinex(
            bitfinexConfig(
                    websocketConnectionURL = "wss://api2.bitfinex.com:3000/ws",
                    publicKey = "8SOAdEL7gPLgB0zz7KiqdOqmIHMw5vfgRnVlFbytfKa",
                    privateKey = "a9pZv0zDvprfK0PvUF7wKZGzeU16m06ZvM7CHQRfncY"
            )
    )

    bitfinex.symbols().forEach { println(it.json()) }

    bitfinex.streamOrders(pair("BTC","USD"))

    readLine()
}
