package btce

import common.IAccount
import common.IExchange
import common.IMarket
import proto.common.Pair

class Btce : IExchange {
    override fun name(): String {
        throw UnsupportedOperationException()
    }

    override fun pairs(): List<Pair> {
        throw UnsupportedOperationException()
    }

    override fun market(pair: Pair): IMarket {
        throw UnsupportedOperationException()
    }

    override fun account(): IAccount {
        throw UnsupportedOperationException()
    }
}