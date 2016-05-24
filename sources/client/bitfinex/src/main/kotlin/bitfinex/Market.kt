package bitfinex

import com.google.gson.JsonArray
import com.tars.util.net.ws.WebsocketClient
import common.IExchange
import common.IMarket
import common.IOrderBook
import proto.bitfinex.ProtoBitfinex
import proto.common.Order
import proto.common.Pair
import proto.common.Trade
import rx.Observable
import rx.subjects.PublishSubject
import util.cpu
import util.global.logger
import util.global.wtf

internal class Market(val exchange: Bitfinex, val pair: Pair) : IMarket {
    internal val log by logger("bitfinex")

    val trades: PublishSubject<Trade>
    val orders: PublishSubject<ProtoBitfinex.Order>

    val ob: OrderBook

    val ws: WebsocketClient
    val channels: MutableMap<Any, (JsonArray) -> Unit>


    init {
        trades = PublishSubject.create()
        orders = PublishSubject.create()
        ob = OrderBook(this)

        orders.subscribe { ob.acceptNext(it) }


        ws = util.net.wsClient(exchange.config.wsURL)
        channels = mutableMapOf()


        channels.put(pair.asTradeKey(), object : (JsonArray) -> Unit {
            override fun invoke(data: JsonArray) {
                parseTrades(data).forEach { trades.onNext(it) }
            }
        })
        channels.put(pair.asBookKey(), object : (JsonArray) -> Unit {
            override fun invoke(data: JsonArray) {
                parseOrders(data).forEach { ob.acceptNext(it) }
            }
        })

        ws.stream().observeOn(cpu.schedulers.io).subscribe(
                {
                    log.debug("| << {}", it)
                    handleMessage(it, channels)
                },
                {
                    log.error("ws client got unexpected exception", it)
                    wtf(it)
                },
                {
                    log.info("websocket client completed")
                }
        )

        ws.start()

        ws.send("{\"event\": \"subscribe\",\"channel\": \"trades\",\"pair\": \"${pair.base.symbol}${pair.quote.symbol}\"}")
        ws.send("{\"event\":\"subscribe\",\"channel\":\"book\",\"pair\":\"${pair.base.symbol}${pair.quote.symbol}\",\"prec\":\"R0\",\"len\":\"full\"}")
    }

    override fun exchange(): IExchange {
        return exchange
    }

    override fun pair(): Pair {
        return pair
    }

    override fun orderbook(): IOrderBook {
        return ob
    }

    override fun streamOrders(): Observable<Order> {
        return ob.stream()
    }

    override fun streamTrades(): Observable<Trade> {
        return trades
    }

}