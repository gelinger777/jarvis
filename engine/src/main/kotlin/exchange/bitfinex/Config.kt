package exchange.bitfinex

import com.tars.util.Util.absolutePathOf
import util.folderName
import proto.Messages
import java.io.File.separator

/**
 * Configuration for bitfinex exchange
 */
data class Config(
        var publicKey: String = "",
        var privateKey: String = "",
        var dataPath: String = "",
        var trade: MutableMap<String, Int> = mutableMapOf<String, Int>(),
        var book: MutableMap<String, Int> = mutableMapOf<String, Int>()
) {
    fun tradeDataPath(pair: Messages.Pair): String {
        // ..\btc-usd\trades\data.*
        return absolutePathOf(dataPath + separator + pair.folderName() + separator + "trades" + separator + "data")
    }

    fun bookDataPath(pair: Messages.Pair): String {
        // ..\btc-usd\book\data.*
        return absolutePathOf(dataPath + separator + pair.folderName() + separator + "book" + separator + "data")
    }
}