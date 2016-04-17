package util

import proto.*
import java.util.concurrent.ConcurrentHashMap

object repo {

    private val currencies = ConcurrentHashMap<String, Currency>()
    private val pairs = ConcurrentHashMap<String, Pair>()

    fun currency(symbol: String): Currency {
        return currencies.computeIfAbsent(symbol, { Currency.newBuilder().setSymbol(it).build() })
    }

    fun pair(base: String, quote: String): Pair {
        return pairs.computeIfAbsent("$base|$quote", {
            Pair.newBuilder()
                    .setBase(currency(base))
                    .setQuote(currency(quote))
                    .build()
        })
    }

    fun trade(price: Double, volume: Double, time: Long): Trade {
        return Trade.newBuilder()
                .setPrice(price)
                .setVolume(volume)
                .setTime(time)
                .build()
    }

    fun order(side: Side, price: Double, volume: Double, id: Long = 0, time: Long = 0): Order {
        return Order.newBuilder()
                .setPrice(price)
                .setVolume(volume)
                .setSide(side)
                .setId(id)
                .setTime(time)
                .build();
    }

}