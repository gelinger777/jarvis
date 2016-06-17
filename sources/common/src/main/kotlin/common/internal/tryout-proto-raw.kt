package common.internal

import proto.common.Order
import proto.common.Raw
import proto.common.Trade
import util.app

internal fun main(args: Array<String>) {

    Trade.newBuilder()
            .setTime(Int.MAX_VALUE.toLong())
            .setPrice(Float.MAX_VALUE.toDouble())
            .setVolume(Float.MAX_VALUE.toDouble())
            .build()
            .apply { app.log.info { "trade : ${this.toByteArray().size} bytes" } }

    Order.newBuilder()
            .setTime(Int.MAX_VALUE.toLong())
            .setPrice(Float.MAX_VALUE.toDouble())
            .setVolume(Float.MAX_VALUE.toDouble())
            .setSide(Order.Side.ASK)
            .build()
            .apply { app.log.info { "order : ${this.toByteArray().size} bytes" } }

    Raw.newBuilder()
            .setTime(Int.MAX_VALUE)
            .setPrice(Float.MAX_VALUE)
            .setVolume(Float.MAX_VALUE)
            .build()
            .apply { app.log.info { "raw : ${this.toByteArray().size} bytes" } }
}