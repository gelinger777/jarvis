package btce

import btce.internal.parsePairs
import common.IAccount
import common.IExchange
import common.IMarket
import proto.bitfinex.ProtoBtce.BtceConfig
import proto.common.Pair

class Btce(val config : BtceConfig) : IExchange {
    override fun name(): String {
        return "BTCE"
    }

    override fun pairs(): List<Pair> {
        return util.net.http.get("https://btc-e.com/api/3/info")
                .map { parsePairs(it) }
                .ifNotPresentCompute { emptyList() }
                .get()
    }

    override fun market(pair: Pair): IMarket {
        throw UnsupportedOperationException()
    }

    override fun account(): IAccount {
        throw UnsupportedOperationException()
    }
}