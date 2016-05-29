package btce

import common.IExchange
import common.IMarket
import common.IOrderBook
import proto.common.Order
import proto.common.Pair
import proto.common.Trade
import rx.Observable

internal class Market :IMarket{
    override fun exchange(): IExchange {
        throw UnsupportedOperationException()
    }

    override fun pair(): Pair {
        throw UnsupportedOperationException()
    }

    override fun orderbook(): IOrderBook {
        throw UnsupportedOperationException()
    }

    override fun trades(): Observable<Trade> {
        throw UnsupportedOperationException()
    }

    override fun orders(): Observable<Order> {
        throw UnsupportedOperationException()
    }
}