package bitfinex.channel;

import bitfinex.channel.BroadCoastingChannel
import com.google.gson.JsonArray
import common.util.order
import proto.common.Order
import proto.common.Order.Side.ASK
import proto.common.Order.Side.BID
import util.global.condition
import util.heartBeat

/**
 * Represents realtime data channel, many observers can subscribe for the channel.
 */
internal class BookChannel : BroadCoastingChannel<Order> {

    constructor(name: String) : super(name) {
    }

    override fun parse(array: JsonArray) {
        heartBeat.beat(name)

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

                val order = order(id, side, price, volume)
                subject.onNext(order)
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

                val order = order(id, side, price, volume)
                subject.onNext(order)
            } else {
                // ensure it was heart beat
                condition(primitive.isString && primitive.asString == "hb")
            }
        }
    }
}