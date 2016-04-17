package bitfinex;

import com.google.gson.JsonArray
import com.tars.util.validation.Validator.condition
import proto.Order
import proto.Pair
import proto.Side.ASK
import proto.Side.BID
import rx.subjects.PublishSubject
import util.repo.order

internal data class BookChannel(
        val name: String,
        val pair: Pair,
        var id: Int = 0,
        val stream: PublishSubject<Order> = PublishSubject.create()
) {
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

                stream.onNext(order(side, price, volume, id))
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

                stream.onNext(order(side, price, volume, id))
            } else {
                // ensure it was heart beat
                condition(primitive.isString && primitive.asString == "hb")
            }

        }
    }
}