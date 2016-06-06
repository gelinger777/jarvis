package common.global

import proto.common.Order
import proto.common.Trade
import rx.subjects.PublishSubject
import java.util.concurrent.ThreadLocalRandom

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
