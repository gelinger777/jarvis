package collector.bitfinex.server.channel

import com.google.gson.JsonArray
import proto.common.Trade

/**
 * Represents realtime data channel, many observers can subscribe for the channel.
 */
internal class TradeChannel : BroadCoastingChannel<Trade> {
    constructor(name: String) : super(name) {
    }

    override fun parse(array: JsonArray) {
        val secondElement = array.get(1)
        if (!secondElement.isJsonArray && secondElement.asString == "te") {
            // new trade
            val trade = Trade.newBuilder()
                    .setTime(array.get(3).asLong * 1000) // unix timestamp to normal
                    .setPrice(array.get(4).asDouble)
                    .setVolume(Math.abs(array.get(5).asDouble))
                    .build()

            observers.forEach { it.onNext(trade) }
        } else {
            // snapshot (we don't need trade snapshot)
        }
    }
}