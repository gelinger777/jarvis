package bitfinex

import com.google.gson.JsonArray
import proto.Pair
import proto.Trade
import rx.subjects.PublishSubject
import util.heartBeat
import util.trade

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

            val trade = trade(
                    price = array.get(4).asDouble,
                    volume = Math.abs(array.get(5).asDouble),
                    time = array.get(3).asLong * 1000
            )

            stream.onNext(trade)
        } else {
            // snapshot
        }
    }
}