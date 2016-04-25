package rpc.server.channel;

import com.google.gson.JsonArray
import com.tars.util.validation.Validator.condition
import io.grpc.stub.StreamObserver
import proto.Order
import proto.Order.Side.ASK
import proto.Order.Side.BID
import proto.Pair
import rpc.server.asBookKey
import util.order

/**
 * Represents realtime data channel, many observers can subscribe for the channel.
 */
internal data class BookChannel(
        val pair: Pair,
        val name: String = pair.asBookKey(),
        val observers: MutableSet<StreamObserver<Order>> = mutableSetOf()
) {

    fun addObserver(observer: StreamObserver<Order>) {
        observers.add(observer)
    }

    fun complete() {
        observers.forEach { it.onCompleted() }
    }

    fun removeObserver(observer: StreamObserver<Order>) {
        observers.remove(observer);
        observer.onCompleted()
    }

    fun parseBook(array: JsonArray) {
        // either heartbeat or single order
        val element = array.get(1)

        // check if its the first batch
        if (element.isJsonArray) {

            // add all orders in batch
            element.asJsonArray.forEach { jsonOrder ->
                val orderArray = jsonOrder.asJsonArray
                val id = orderArray.get(0).asLong
                val price = orderArray.get(1).asDouble
                var volume = orderArray.get(2).asDouble
                val side = if (volume > 0) BID else ASK

                // normalize negative volumes
                if (volume < 0) {
                    volume = -volume;
                }

                // normalize volume for cancelation orders
                if (price == 0.0 && volume == 1.0) {
                    volume = 0.0;
                }

                val order = order(id, side, price, volume, 0) // todo

                observers.forEach { it.onNext(order) }
            }

        } else if (element.isJsonPrimitive) {
            val primitive = element.asJsonPrimitive

            if (primitive.isNumber) {
                val id = primitive.asLong;
                val price = array.asJsonArray.get(2).asDouble
                var volume = array.asJsonArray.get(3).asDouble
                val side = if (volume > 0) BID else ASK

                // normalize negative volumes
                if (volume < 0) {
                    volume = -volume;
                }

                // normalize volume for cancellation orders
                if (price == 0.0 && volume == 1.0) {
                    volume = 0.0;
                }

                val order = order(id, side, price, volume, 0) // todo
                observers.forEach { it.onNext(order) }
            } else {
                // ensure it was heart beat
                condition(primitive.isString && primitive.asString == "hb")
            }

        }
    }
}