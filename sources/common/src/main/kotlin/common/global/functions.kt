package common.global

import proto.common.Currency
import proto.common.Order
import proto.common.Pair
import proto.common.Trade
import rx.subjects.PublishSubject
import util.global.condition
import util.global.executeAndGetMandatory
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ThreadLocalRandom


private object repo {
    val currencies = ConcurrentHashMap<String, Currency>()
    val pairs = ConcurrentHashMap<String, Pair>()
}

object protoRandom {

    val trades = PublishSubject.create<Trade>()
    val orders = PublishSubject.create<Order>()

    fun randomTrade(): Trade = trade(
            price = randomPrice(),
            volume = randomVolume(),
            time = randomTime()
    )

    fun nextTrade(price: Double = randomPrice(), volume: Double = randomVolume(), time: Long = randomTime()) = trades.onNext(trade(price, volume, time))

    fun randomOrder(): Order = order(
            side = randomSide(),
            price = randomPrice(),
            volume = randomVolume(),
            time = randomTime()
    )

    fun nextOrder(side: Order.Side = randomSide(), price: Double = randomPrice(), volume: Double = randomVolume(), time: Long = randomTime()) = orders.onNext(order(side, price, volume, time))

    // stuff

    private fun randomSide(): Order.Side = if (random().nextDouble() > 0.5) Order.Side.BID else Order.Side.ASK

    private fun randomPrice(): Double = random().nextDouble(410.0, 420.0)

    private fun randomVolume(): Double = random().nextDouble(1.0, 10.0)

    private fun randomTime(): Long = System.currentTimeMillis()

    private fun random(): ThreadLocalRandom = ThreadLocalRandom.current()
}

// base types ==========================================================================

fun currency(symbol: String): Currency {

    val uSymbol = symbol.toLowerCase()

    return repo.currencies.computeIfAbsent(uSymbol, { Currency.newBuilder().setSymbol(it).build() })
}

fun pair(base: String, quote: String): Pair {

    val uBase = base.toLowerCase()
    val uQuote = quote.toLowerCase()

    return repo.pairs.computeIfAbsent("$uBase|$uQuote", {
        Pair.newBuilder()
                .setBase(currency(uBase))
                .setQuote(currency(quote))
                .build()
    })
}

fun trade(price: Double, volume: Double, time: Long): Trade {
    condition(price > 0 && volume > 0 && time > 0)
    return Trade.newBuilder()
            .setPrice(price)
            .setVolume(volume)
            .setTime(time)
            .build()
}

fun trade(data: ByteArray): Trade {
    return executeAndGetMandatory { Trade.parseFrom(data) }
}

fun order(side: Order.Side, price: Double, volume: Double, time: Long = System.currentTimeMillis()): Order {
    condition(time > 0 && price > 0 && volume >= 0)
    return Order.newBuilder()
            .setTime(time)
            .setSide(side)
            .setPrice(price)
            .setVolume(volume)
            .build();
}

fun order(data: ByteArray): Order {
    return executeAndGetMandatory { Order.parseFrom(data) }
}