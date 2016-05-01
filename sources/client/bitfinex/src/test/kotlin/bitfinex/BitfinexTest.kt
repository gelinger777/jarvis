package bitfinex

import collector.bitfinex.server.BitfinexConfig
import common.util.json
import common.util.pair
import org.junit.Test
import util.cpu
import java.util.concurrent.TimeUnit.MINUTES

class BitfinexTest {
    val bitfinex = Bitfinex(
            BitfinexConfig(
                    websocketConnectionURL = "wss://api2.bitfinex.com:3000/ws",
                    publicKey = "8SOAdEL7gPLgB0zz7KiqdOqmIHMw5vfgRnVlFbytfKa",
                    privateKey = "a9pZv0zDvprfK0PvUF7wKZGzeU16m06ZvM7CHQRfncY"
            )
    )

    @Test
    fun symbols() {
        bitfinex.symbols().forEach { println(it.json()) }
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