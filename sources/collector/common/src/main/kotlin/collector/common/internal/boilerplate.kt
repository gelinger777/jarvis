package collector.common.internal

import common.IExchange
import proto.common.Pair


internal fun ordersRelativePath(client: IExchange, pair: Pair): String {
    return "${client.name()}/${pair.base.symbol.toLowerCase()}-${pair.quote.symbol.toLowerCase()}/orders"

}

internal fun tradesRelativePath(client: IExchange, pair: Pair): String {
    return "${client.name()}/${pair.base.symbol.toLowerCase()}-${pair.quote.symbol.toLowerCase()}/trades"
}
