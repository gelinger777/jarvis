package collector.bitfinex.server

import bitfinex.Bitfinex
import global.logger
import io.grpc.stub.StreamObserver
import org.apache.http.client.methods.RequestBuilder.get
import proto.common.*
import util.asFolderName
import util.asPair
import util.net.http
import util.respondCollShutdown
import util.respondCollStatus
import java.io.File

internal class BitfinexServiceNew(val config: BitfinexConfig) : CollectorGrpc.Collector {
    val log by logger("bitfinex-collector-server")

    val bitfinex = Bitfinex(config)
    val recorders = mutableMapOf<String, Any>()


    override fun status(request: CollStatusReq, observer: StreamObserver<CollStatusResp>) {
        log.debug("getting accessible market pairs")


        val supportedPairs = http.getString(get("https://api.bitfinex.com/v1/symbols"))
                .map { response ->
                    response.replace(Regex("\\[|\\]|\\n|\""), "")
                            .split(",")
                            .asSequence()
                            .map { str -> str.asPair() }
                            .toList()
                }
                .ifNotPresentCompute { emptyList<Pair>() }
                .get()

        respondCollStatus(observer, supportedPairs)
    }

    override fun shutdown(request: CollShutdownReq, observer: StreamObserver<CollShutdownResp>) {
//        channels.values.forEach { it.complete() }
//        channels.clear()
//
//        websocketClient.stop()

        respondCollShutdown(observer, success = false)
    }

    override fun streamTrades(request: StreamTradesReq, observer: StreamObserver<Trade>) {
//        val pair = request.pair
//        // get channel or create if necessary
//        val channel = channels.computeIfAbsent(pair.asTradeKey(), {
//            log.info("starting trade stream : {}", pair.json());
//
//            // send subscription request to bitfinex
//            websocketClient.send("{\"event\": \"subscribe\",\"channel\": \"trades\",\"pair\": \"${pair.base.symbol}${pair.quote.symbol}\"}")
//            // reserve a channel
//            TradeChannel(pair.asTradeKey())
//        })
//
//        // register new observer
//        (channel as TradeChannel).addObserver(observer)
    }

    override fun streamOrders(request: StreamOrdersReq, observer: StreamObserver<Order>) {
//        val pair = request.pair
//
//        // get channel or create if necessary
//        val channel = channels.computeIfAbsent(pair.asBookKey(), {
//            log.info("starting orderbook stream : {}", pair.json());
//
//            // send subscription request to bitfinex
//            websocketClient.send("{\"event\":\"subscribe\",\"channel\":\"book\",\"pair\":\"${pair.base.symbol}${pair.quote.symbol}\",\"prec\":\"R0\",\"len\":\"full\"}")
//            // reserve a channel
//            BookChannel(pair.asBookKey())
//        })
//
//        // register new observer
//        (channel as BookChannel).addObserver(observer)
    }

    override fun recordTrades(request: RecordTradesReq, observer: StreamObserver<RecordTradesResp>) {
//        val pair = request.pair
//
//        recorders.computeIfAbsent(config.tradeDataPath(pair), {
//            // create recording channel
//            val recorder = RecordingObserver<Trade>(it)
//            // stream trades to recorder
//            streamTrades(requestStreamTrades(pair), recorder)
//            recorder
//        })
//
//        respondRecordTrades(observer, success = true)

    }

    override fun recordOrders(request: RecordOrdersReq, observer: StreamObserver<RecordOrdersResp>) {
//        val pair = request.pair
//
//        recorders.computeIfAbsent(config.bookDataPath(pair), {
//            // create recording channel
//            val recorder = RecordingObserver<Order>(it)
//
//            streamOrders(requestStreamOrders(pair), recorder)
//            recorder
//        })
//
//        respondRecordOrders(observer, success = true)
    }


    override fun streamHistoricalTrades(request: StreamHistoricalTradesReq, observer: StreamObserver<Trade>) {
//        val path = config.tradeDataPath(request.pair)
//        val stream = storage.eventStream(path)
//
//        stream.stream(request.startIndex, request.endIndex).subscribe { observer.onNext(Trade.parseFrom(it)) }
//        observer.onCompleted()
    }

    override fun streamHistoricalOrders(request: StreamHistoricalOrdersReq, observer: StreamObserver<Order>) {
//        val path = config.bookDataPath(request.pair)
//        val stream = storage.eventStream(path)
//
//        stream.stream(request.startIndex, request.endIndex).subscribe { observer.onNext(Order.parseFrom(it)) }
//        observer.onCompleted()
    }



    fun tradeDataPath(pair: Pair): String {
        // btc-usd\trades\data.*
        return pair.asFolderName() + File.separator + "trades" + File.separator + "data"
    }

    fun ordersDataPath(pair: Pair): String {
        // btc-usd\book\data.*
        return pair.asFolderName() + File.separator + "orders" + File.separator + "data"
    }
}

