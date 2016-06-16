package bitfinex

import common.IAccount
import common.IExchange
import common.IMarket
import common.global.asPair
import common.global.compact
import proto.common.Pair
import util.global.computeIfAbsent
import util.global.condition
import util.global.logger
import util.global.notImplemented
import util.net

class Bitfinex() : IExchange {
    internal val log = logger("bitfinex")

    val name = "bitfinex"
    internal val markets = mutableMapOf<Pair, Market>()

    override fun name(): String {
        return name
    }

    override fun pairs(): List<Pair> {
        log.info("getting accessible market pairs")

        return net.http.get("https://api.bitfinex.com/v1/symbols")
                .map { response ->
                    response.replace(Regex("\\[|\\]|\\n|\""), "")
                            .split(",")
                            .map { str -> str.asPair() }
                            .toList()
                }
                .ifNotPresentCompute { emptyList<Pair>() }
                .get()
    }

    override @Synchronized fun market(pair: Pair): IMarket {
        condition(pairs().contains(pair), "${pair.compact()} is not supported by ${name}")
        return markets.computeIfAbsent(pair, { Market(this, it) })
    }

    override fun account(): IAccount {
        notImplemented()
    }
}

