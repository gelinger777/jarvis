package gdax.internal

import util.app.log


fun getPairs() {
    util.net.http.get("https://api-public.sandbox.gdax.com/products").ifPresent { log.info { it } }
}

internal fun main(args: Array<String>) {
    getPairs()
}