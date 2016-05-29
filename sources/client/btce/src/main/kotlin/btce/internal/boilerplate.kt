package btce.internal

import btce.Btce
import com.google.gson.JsonParser
import common.global.asPair
import proto.common.Pair


internal fun Btce.parsePairs(json: String): List<Pair> {
    return JsonParser().parse(json).asJsonObject.getAsJsonObject("pairs")
            .entrySet().map { it.key.asPair() }
            .toList()
}