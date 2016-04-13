package util

import proto.Messages
import java.util.concurrent.ConcurrentHashMap

object repo {

    private val currencies = ConcurrentHashMap<String, Messages.Currency>()
    private val pairs = ConcurrentHashMap<String, Messages.Pair>()

    fun currency(symbol: String): Messages.Currency {
        return currencies.computeIfAbsent(symbol, { Messages.Currency.newBuilder().setSymbol(it).build() })
    }

    fun pair(base: String, quote: String): Messages.Pair {
        return pairs.computeIfAbsent("$base|$quote", {
            Messages.Pair.newBuilder()
                    .setBase(currency(base))
                    .setQuote(currency(quote))
                    .build()
        })
    }

    fun trade(price: Double, volume: Double, time: Long): Messages.Trade {
        return Messages.Trade.newBuilder()
                .setPrice(price)
                .setVolume(volume)
                .setTime(time)
                .build()
    }

    fun order(side: Messages.Side, price: Double, volume: Double, id: Long = 0, time: Long = 0): Messages.Order {
        return Messages.Order.newBuilder()
                .setPrice(price)
                .setVolume(volume)
                .setSide(side)
                .setId(id)
                .setTime(time)
                .build();
    }

}