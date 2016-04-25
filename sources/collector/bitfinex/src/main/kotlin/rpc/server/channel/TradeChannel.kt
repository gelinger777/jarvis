package rpc.server.channel

import com.google.gson.JsonArray
import io.grpc.stub.StreamObserver
import proto.Pair
import proto.Trade
import rpc.server.asTradeKey

/**
 * Represents realtime data channel, many observers can subscribe for the channel.
 */
internal data class TradeChannel(
        val pair: Pair,
        val name: String = pair.asTradeKey(),
        val observers: MutableSet<StreamObserver<Trade>> = mutableSetOf()
) {
    fun addObserver(observer: StreamObserver<Trade>) {
        observers.add(observer)
    }

    fun complete() {
        observers.forEach { it.onCompleted() }
    }

    fun removeObserver(observer: StreamObserver<Trade>) {
        observers.remove(observer);
        observer.onCompleted()
    }

    fun parseTrade(array: JsonArray) {
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