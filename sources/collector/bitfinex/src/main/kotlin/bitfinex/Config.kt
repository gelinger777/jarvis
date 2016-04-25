package bitfinex

import com.tars.util.Util.absolutePathOf
import proto.Pair
import util.asFolderName
import java.io.File.separator

/**
 * Configuration for bitfinex exchange
 */
internal data class Config(
        var websocketConnectionURL : String = "",
        var publicKey: String = "",
        var privateKey: String = "",
        var dataPath: String = "",
        var trade: MutableMap<String, Int> = mutableMapOf<String, Int>(),
        var book: MutableMap<String, Int> = mutableMapOf<String, Int>()
) {
    fun tradeDataPath(pair: Pair): String {
        // ..\btc-usd\trades\data.*
        return absolutePathOf(dataPath + separator + pair.asFolderName() + separator + "trades" + separator + "data")
    }

    fun bookDataPath(pair: Pair): String {
        // ..\btc-usd\book\data.*
        return absolutePathOf(dataPath + separator + pair.asFolderName() + separator + "book" + separator + "data")
    }
}