package client.btce

import client.btce.internal.pollPairs
import common.IAccount
import common.IExchange
import common.IMarket
import common.global.compact
import proto.common.Pair
import util.global.computeIfAbsent
import util.global.condition
import util.global.logger

class Btce() : IExchange {
    val log = logger("btce")

    val name = "btce"
    internal val markets = mutableMapOf<Pair, Market>()

    override fun name(): String {
        return name
    }

    override fun pairs(): List<Pair> {
        return pollPairs().ifNotPresentCompute { emptyList() }.get()
    }

    override fun market(pair: Pair): IMarket {
        condition(pairs().contains(pair), "${pair.compact()} is not supported")
        return markets.computeIfAbsent(pair, { Market(this, it) })
    }

    override fun account(): IAccount {
        throw UnsupportedOperationException()
    }
}