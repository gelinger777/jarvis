package collector.bitfinex.server

import proto.common.Pair
import util.asKey


fun Pair.asTradeKey(): String {
    return "TRADE|${this.asKey()}";
}

fun Pair.asBookKey(): String {
    return "BOOK|${this.asKey()}";
}