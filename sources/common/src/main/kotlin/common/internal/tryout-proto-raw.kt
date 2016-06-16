package common.internal

import proto.common.Order
import proto.common.Raw
import proto.common.Trade

internal fun main(args: Array<String>) {

    Trade.newBuilder()
            .setTime(Int.MAX_VALUE.toLong())
            .setPrice(Float.MAX_VALUE.toDouble())
            .setVolume(Float.MAX_VALUE.toDouble())
            .build()
            .apply { println("trade : ${this.toByteArray().size}") }

    Order.newBuilder()
            .setTime(Int.MAX_VALUE.toLong())
            .setPrice(Float.MAX_VALUE.toDouble())
            .setVolume(Float.MAX_VALUE.toDouble())
            .setSide(Order.Side.ASK)
            .build()
            .apply { println("order : ${this.toByteArray().size}") }

    Raw.newBuilder()
            .setTime(Int.MAX_VALUE)
            .setPrice(Float.MAX_VALUE)
            .setVolume(Float.MAX_VALUE)
            .build()
            .apply { println("raw : ${this.toByteArray().size}") }
}