package collector.bitfinex.server.channel

import com.google.gson.JsonArray
import proto.common.Trade
import util.heartBeat
import common.trade

/**
 * Represents realtime data channel, many observers can subscribe for the channel.
 */
internal class TradeChannel : BroadCoastingChannel<Trade> {
    constructor(name: String) : super(name) {
    }

    override fun parse(array: JsonArray) {
        try {
            heartBeat.beat(name)

            val secondElement = array.get(1)
            if (!secondElement.isJsonArray && secondElement.asString == "te") {

                // new trade
                val price = array.get(4).asDouble
                val volume = Math.abs(array.get(5).asDouble)
                val time = array.get(3).asLong * 1000

                val trade = trade(price, volume, time)

                subject.onNext(trade)
            } else {
                // snapshot (we don't need trade snapshot)
            }

        } catch(error: Throwable) {
            subject.onError(error)
        }
    }
}