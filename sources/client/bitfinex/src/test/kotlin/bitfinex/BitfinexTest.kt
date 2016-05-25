package bitfinex

import common.global.bitfinexConfig
import common.global.json
import common.global.pair
import org.junit.Test
import util.cpu
import java.util.concurrent.TimeUnit.MINUTES

class BitfinexTest {
    val bitfinex = Bitfinex(
            bitfinexConfig(
                    wsUrl = "wss://api2.bitfinex.com:3000/ws",
                    publicKey = "8SOAdEL7gPLgB0zz7KiqdOqmIHMw5vfgRnVlFbytfKa",
                    privateKey = "a9pZv0zDvprfK0PvUF7wKZGzeU16m06ZvM7CHQRfncY"
            )
    )

    @Test
    fun symbols() {
        bitfinex.pairs().forEach { println(it.json()) }
    }

    @Test
    fun tradeStream() {
        bitfinex.streamTrades(pair("BTC", "USD")).subscribe { println(it.json()) }

        cpu.sleep(1, MINUTES)
    }

    @Test
    fun orderStream() {
        bitfinex.streamOrders(pair("BTC", "USD")).subscribe { println(it.json()) }

        cpu.sleep(1, MINUTES)
    }

}