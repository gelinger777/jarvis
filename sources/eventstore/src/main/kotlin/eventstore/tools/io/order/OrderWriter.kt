package eventstore.tools.io.order

import com.google.common.primitives.Longs
import common.global.json
import eventstore.tools.io.bytes.BytesWriter
import proto.common.Order
import proto.common.Order.Side.ASK
import proto.common.Order.Side.BID
import proto.common.Raw
import util.app
import util.global.wtf

class OrderWriter(val rawWriter: BytesWriter) {

    var lastTime = -1L

    @Synchronized fun write(order: Order) {


        app.log.info { "write : " + order.json() }

        // write initial timestamp on first write
        if (lastTime == -1L) {
            lastTime = order.time
            rawWriter.write(Longs.toByteArray(lastTime))
        }

        // calculate time difference since last time
        val time = (order.time -lastTime).toInt()
        val price = order.price.toFloat()
        val vol = when (order.side) {
            BID -> order.volume.toFloat()
            ASK -> -order.volume.toFloat()
            else -> wtf()
        }

        rawWriter.write(Raw.newBuilder().setTime(time).setPrice(price).setVolume(vol).build().toByteArray())

        lastTime = order.time
    }

}
