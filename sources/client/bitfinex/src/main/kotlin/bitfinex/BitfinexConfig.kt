package collector.bitfinex.server

/**
 * Configuration for bitfinex exchange
 */
data class BitfinexConfig(
        var websocketConnectionURL: String = "",
        var publicKey: String = "",
        var privateKey: String = "")

//    fun tradeDataPath(pair: Pair): String {
//        // ..\btc-usd\trades\data.*
//        return dataPath + separator + pair.asFolderName() + separator + "trades" + separator + "data"
//    }
//
//    fun bookDataPath(pair: Pair): String {
//        // ..\btc-usd\book\data.*
//        return dataPath + separator + pair.asFolderName() + separator + "book" + separator + "data"
//    }
