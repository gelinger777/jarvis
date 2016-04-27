package collector.bitfinex.server

import proto.common.Pair
import util.asFolderName
import java.io.File.separator

/**
 * Configuration for bitfinex exchange
 */
internal data class Config(
        var websocketConnectionURL: String = "",
        var publicKey: String = "",
        var privateKey: String = "",
        var dataPath: String = "",
        var trade: MutableMap<String, Int> = mutableMapOf<String, Int>(),
        var book: MutableMap<String, Int> = mutableMapOf<String, Int>()
) {

}