package bitfinex

import com.google.gson.JsonArray
import proto.Pair
import proto.Trade
import rx.subjects.PublishSubject
import util.heartBeat

internal data class TradeChannel(
        val name: String,
        val pair: Pair,
        var id: Int = 0,
        val stream: PublishSubject<Trade> = PublishSubject.create()
) {
    fun parseTrade(array: JsonArray) {
        // heartbeat
        if (array.size() == 2) {
            heartBeat.beat(name)
        }

        val secondElement = array.get(1)
        if (!secondElement.isJsonArray && secondElement.asString == "te") {
            // new trade
            val trade = Trade.newBuilder()
                    .setTime(array.get(3).asLong * 1000) // unix timestamp to normal
                    .setPrice(array.get(4).asDouble)
                    .setVolume(Math.abs(array.get(5).asDouble))
                    .build()

            stream.onNext(trade)
        } else {
            // snapshot
        }
    }
}