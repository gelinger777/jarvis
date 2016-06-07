package collector.common.internal

import collector.common.server.CollectorService
import proto.common.Pair
import java.io.File

internal fun CollectorService.tradeDataPathFor(pair: Pair): String {
    // exchange/btc-usd/trades
    return client.name().toLowerCase() + File.separator + pair.asFolderName() + File.separator + "trades"
}

internal fun CollectorService.ordersDataPathFor(pair : Pair): String {
    // exchange/btc-usd/orders
    return client.name().toLowerCase() + File.separator + pair.asFolderName() + File.separator + "orders"
}

private fun Pair.asFolderName(): String {
    // btc-usd
    return "${base.symbol.toLowerCase()}-${quote.symbol.toLowerCase()}"
}
