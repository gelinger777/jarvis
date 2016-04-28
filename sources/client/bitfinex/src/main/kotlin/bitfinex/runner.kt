package bitfinex

import collector.bitfinex.server.BitfinexConfig
import common.util.json

fun main(args: Array<String>) {

    val bitfinex = Bitfinex(
            BitfinexConfig(
                    websocketConnectionURL = "wss://api2.bitfinex.com:3000/ws",
                    publicKey = "8SOAdEL7gPLgB0zz7KiqdOqmIHMw5vfgRnVlFbytfKa",
                    privateKey = "a9pZv0zDvprfK0PvUF7wKZGzeU16m06ZvM7CHQRfncY"
            )
    )

    bitfinex.symbols().forEach { println(it.json()) }


}
