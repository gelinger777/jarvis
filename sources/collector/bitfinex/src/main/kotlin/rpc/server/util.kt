package rpc.server

import proto.Pair
import util.asKey


fun Pair.asTradeKey(): String {
    return "TRADE|${this.asKey()}";
}

fun Pair.asBookKey(): String {
    return "BOOK|${this.asKey()}";
}