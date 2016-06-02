package bitstamp

import common.IAccount
import common.IExchange
import common.IMarket
import common.global.pair
import proto.bitstamp.ProtoBitstamp.BitstampConfig
import proto.common.Pair
import util.global.computeIfAbsent
import util.global.condition
import util.global.logger
import util.global.notImplemented


class Bitstamp(val config: BitstampConfig) : IExchange {
    internal val log by logger("bitstamp")

    internal val markets = mutableMapOf<Pair, Market>()

    override fun name(): String {
        return "BITSTAMP"
    }

    override fun pairs(): List<Pair> {
        log.info("getting accessible market pairs")

            return mutableListOf(
                    pair("btc", "usd"),
                    pair("btc", "eur")
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