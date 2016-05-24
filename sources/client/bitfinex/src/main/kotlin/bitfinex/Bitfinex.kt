package bitfinex

import common.IAccount
import common.IExchange
import common.IMarket
import common.util.pair
import proto.bitfinex.ProtoBitfinex.BitfinexConfig
import proto.common.Pair
import util.global.computeIfAbsent
import util.global.condition
import util.global.logger
import util.global.notImplemented

class Bitfinex(val config: BitfinexConfig) : IExchange {
    internal val log by logger("bitfinex")

    internal val markets: MutableMap<Pair, Market>


    init {
        log.info("starting")

        markets = mutableMapOf()
    }

    override fun name(): String {
        return "BITFINEX"
    }

    override fun pairs(): List<Pair> {
        log.info("getting accessible market pairs")

//        return http.getString(get("https://api.bitfinex.com/v1/symbols"))
//                .map { response ->
//                    response.replace(Regex("\\[|\\]|\\n|\""), "")
//                            .split(",")
//                            .asSequence()
//                            .map { str -> str.asPair() }
//                            .toList()
//                }
//                .ifNotPresentCompute { emptyList<Pair>() }
//                .get()

        // remove this shit later

        return mutableListOf(
                pair("btc", "usd"),
                pair("ltc", "usd"),
                pair("ltc", "btc"),
                pair("eth", "usd"),
                pair("eth", "btc")
        )
    }

    override @Synchronized fun market(pair: Pair): IMarket {
        condition(pairs().contains(pair))
        return markets.computeIfAbsent(pair, { Market(this, it) })
    }

    override fun account(): IAccount {
        return notImplemented()
    }
}

