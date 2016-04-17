import global.getOptional
import global.logger
import util.Option


fun main(args: Array<String>) {
    val mutableMapOf = mutableMapOf<Any, Any>()

    val optional : Option<Any> = mutableMapOf.getOptional("key")


}