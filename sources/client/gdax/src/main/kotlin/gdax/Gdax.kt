package gdax

import common.IAccount
import common.IExchange
import common.IMarket
import proto.common.Pair

// todo
class Gdax : IExchange {
    // Sandbox
    val sandbox = "https://public.sandbox.gdax.com"

    // REST API
    val rest = "https://api-public.sandbox.gdax.com"

    // Websocket Feed
    val ws = "wss://ws-feed-public.sandbox.gdax.com"

    // FIX API
    val fix = "https://fix-public.sandbox.gdax.com"


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