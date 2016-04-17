package common

import proto.*
import rx.Observable

interface GenericCollector {

    // getting info about collector service

    /**
     * Get supported markets on the exchange.
     */
    fun accessibleMarketPairs() : List<Pair>

    /**
     * Trade streams that service records historical data for.
     */
    fun getCurrentlyRecordingTradePairs(): List<Pair>

    /**
     * Order streams that service records historical data for.
     */
    fun getCurrentlyRecordingOrderPairs(): List<Pair>

    /**
     * Close all streams gracefully and shut down the service.
     */
    fun shutDown(): ExecutionStatus

    // streaming realtime data

    /**
     * Realtime trade stream for pair.
     */
    fun streamTrades(pair: Pair): Observable<Trade>

    /**
     * Realtime order stream for pair.
     */
    fun streamOrders(pair: Pair): Observable<Order>

    // trade historical data

    /**
     * Information about historical data of trade stream.
     */
    fun getTradeStreamInfo(pair: Pair): TradeStreamInfo

    /**
     * Start recording historical trade data for pair.
     */
    fun startRecordingTrades(pair: Pair): ExecutionStatus

    /**
     * Stop recording historical trade data for pair.
     */
    fun stopRecordingTrades(pair: Pair): ExecutionStatus

    // orderbook historical data

    /**
     * Information about historical data of order stream.
     */
    fun getOrderStreamInfo(pair: Pair): OrderStreamInfo

    /**
     * Start recording historical book data for pair.
     */
    fun startRecordingOrders(pair: Pair): ExecutionStatus

    /**
     * Stop recording historical book data for pair.
     */
    fun stopRecordingOrders(pair: Pair): ExecutionStatus

    // expose historical data trough chronicle TCP connection

    /**
     * Enable stream to be replicated trough TCP (via chronicle sink)
     */
    fun exposeHistoricalTradesData(pair: Pair): InetAddress

    /**
     * Close tcp endpoint.
     */
    fun closeHistoricalTradeData(pair: Pair): ExecutionStatus

    /**
     * Enable stream to be replicated trough TCP (via chronicle sink)
     */
    fun exposeHistoricalBookData(pair: Pair): InetAddress

    /**
     * Close TCP endpoint
     */
    fun closeHistoricalBookData(pair: Pair): ExecutionStatus
}