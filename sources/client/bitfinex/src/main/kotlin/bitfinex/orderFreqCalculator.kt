package bitfinex

import common.util.bitfinexConfig
import common.util.pair
import java.util.concurrent.atomic.AtomicLong

fun main(args: Array<String>) {

    val bitfinex = Bitfinex(
            bitfinexConfig(
                    websocketConnectionURL = "wss://api2.bitfinex.com:3000/ws",
                    publicKey = "8SOAdEL7gPLgB0zz7KiqdOqmIHMw5vfgRnVlFbytfKa",
                    privateKey = "a9pZv0zDvprfK0PvUF7wKZGzeU16m06ZvM7CHQRfncY"
            )
    )


    var counter = AtomicLong(0)

    val stream = bitfinex.streamOrders(pair("BTC", "USD"))


    readLine()
    val startTime = System.currentTimeMillis()
    stream.subscribe {
        println(it.toByteArray().size)
        counter.incrementAndGet()
    }

    readLine()

    val totalTime = System.currentTimeMillis() - startTime
    val totalCount = counter.get()

    println("time : $totalTime, count : $totalCount, speed : ${totalTime / totalCount}")
}
