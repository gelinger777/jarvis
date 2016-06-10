package btce

import btce.internal.pollPairs
import common.IAccount
import common.IExchange
import common.IMarket
import proto.common.Pair
import util.global.computeIfAbsent
import util.global.condition
import util.global.logger

class Btce() : IExchange {
    val log by lazyLogger("btce")

    internal val markets = mutableMapOf<Pair, Market>()

    override fun name(): String {
        return "BTCE"
    }

    override fun pairs(): List<Pair> {
        return pollPairs().ifNotPresentCompute { emptyList() }.get()
    }

    override fun market(pair: Pair): IMarket {
        condition(pairs().contains(pair))
        return markets.computeIfAbsent(pair, { Market(this, it) })
    }

    override fun account(): IAccount {
        throw UnsupportedOperationException()
    }
}