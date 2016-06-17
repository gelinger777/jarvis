package eventstore.tools.io.trade

import com.google.common.primitives.Longs
import common.global.json
import eventstore.tools.io.bytes.BytesWriter
import proto.common.Raw
import proto.common.Trade
import util.app

class TradeWriter(val rawWriter: BytesWriter) {

    var lastTime = -1L

    @Synchronized fun write(trade: Trade) {
        app.log.info { "writing : " + trade.json() }

        // write initial timestamp on first write
        if (lastTime == -1L) {
            lastTime = trade.time
            rawWriter.write(Longs.toByteArray(lastTime))
        }

        // calculate time difference since last time
        val time = (trade.time -lastTime).toInt()
        val price = trade.price.toFloat()
        val vol = trade.volume.toFloat()

        rawWriter.write(Raw.newBuilder().setTime(time).setPrice(price).setVolume(vol).build().toByteArray())

        lastTime = trade.time
    }

}
