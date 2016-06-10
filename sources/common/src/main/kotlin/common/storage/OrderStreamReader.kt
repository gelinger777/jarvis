package common.storage

/**
 * Analyses raw stream of orders and provides api to query historical data. Streaming orders does not make sense if the initial orderbook is not being streamed first, hence it will create different orderbook snapshots.
 *
 * todo : using eventstore-tools
 */
class OrderStreamReader {
}