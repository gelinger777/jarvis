package exchange.bitfinex

import proto.Messages
import proto.Messages.Pair
import proto.Messages.Trade
import rx.Observable

/**
 * Generic api client that shall provide all the necessary functionality for an exchange.
 */
interface GenericClient {

    // lifecycle

    fun start()

    fun stop()

    // trading streams

    fun streamTrades(pair: Pair): Observable<Trade>

    fun activeTradeStreams(): Set<Pair>

    fun closeTradeStream(pair: Pair)
    // trading streams

    fun streamBook(pair: Pair): Observable<Messages.Order>

    fun activeBookStreams(): Set<Pair>

    fun closeBookStream(pair: Pair)
}
