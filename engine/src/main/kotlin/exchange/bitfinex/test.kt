package exchange.bitfinex

import com.tars.util.storage.EventStream
import extensions.batch
import util.json
import util.toOrder
import org.springframework.boot.SpringApplication
import util.repo

fun main(args: Array<String>) {

    val ctx = SpringApplication.run(BitfinexContext::class.java, *args)

    val bitfinex = ctx.getBean(Bitfinex::class.java)

    val pair = repo.pair("BTC", "USD")
    val path = bitfinex.config.tradeDataPath(pair)
    val stream = EventStream.get(path)


    stream.streamFromStart()
            .map { it.toOrder() }
            .subscribe {
                println("Order persisted ${it.json()}")
            }

    bitfinex.streamBook(pair)
            .map { it.toByteArray() }
            .batch()
            .subscribe { batch ->
                for (data in batch) {
                    stream.append(data)
                }
            }

    //    val config = bitfinex.config
    //
    //    log.info("starting trade streams")
    //
    //    config.trade.asSequence().forEach {
    //        val symbol = it.key
    //        val port = it.value
    //
    //        val pair = Util.pair(symbol)
    //        val path = bitfinex.config.tradeDataPath(pair)
    //
    //        val stream = EventStream.get(path, port)
    //
    //        bitfinex.streamTrades(pair)
    //                .map { it.toByteArray() }
    //                .batch()
    //                .subscribe { batch ->
    //                    println("trade batch ${batch.size}")
    //
    //                    for (data in batch) {
    //                        stream.append(data)
    //                    }
    //                }
    //    }
    //
    //    log.info("starting book streams")
    //
    //    config.book.asSequence().forEach {
    //        val symbol = it.key
    //        val port = it.value
    //
    //        val pair = Util.pair(symbol)
    //        val path = bitfinex.config.bookDataPath(pair)
    //
    //        val stream = EventStream.get(path, port)
    //
    //        bitfinex.streamOrderbook(pair)
    //                .map { it.toByteArray() }
    //                .batch()
    //                .subscribe { batch ->
    //                    println("book batch ${batch.size}")
    //
    //                    for (data in batch) {
    //                        stream.append(data)
    //                    }
    //                }
    //    }

//    log.info("enter to stop")

    readLine()

    // release resources and shut down
    ctx.close()
}
