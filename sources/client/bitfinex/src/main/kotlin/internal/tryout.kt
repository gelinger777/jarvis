package internal

import bitfinex.Bitfinex
import common.global.bitfinexConfig
import common.global.json
import common.global.pair

internal fun main(args: Array<String>) {
    val bitfinex = Bitfinex(
            bitfinexConfig(
                    wsUrl = "wss://api2.bitfinex.com:3000/ws",
                    publicKey = "8SOAdEL7gPLgB0zz7KiqdOqmIHMw5vfgRnVlFbytfKa",
                    privateKey = "a9pZv0zDvprfK0PvUF7wKZGzeU16m06ZvM7CHQRfncY"
            )
    )

//    bitfinex.pairs().forEach { println(it.asKey()) }


    bitfinex.market(pair("btc", "usd")).orders().subscribe { println("order : " + it.json()) }
//    bitfinex.market(pair("btc", "usd")).trades().subscribe { println("trade : " + it.json()) }


    readLine()


}