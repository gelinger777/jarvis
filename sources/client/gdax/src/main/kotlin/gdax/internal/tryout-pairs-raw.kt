package gdax.internal


fun getPairs(){
    util.net.http.get("https://api-public.sandbox.gdax.com/products").ifPresent { println(it) }
}

internal fun main(args: Array<String>) {
    getPairs()

}