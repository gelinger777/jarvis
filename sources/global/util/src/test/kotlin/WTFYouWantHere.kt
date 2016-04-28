
import util.Option
import util.global.getOptional


fun main(args: Array<String>) {
    val mutableMapOf = mutableMapOf<Any, Any>()

    val optional : Option<Any> = mutableMapOf.getOptional("key")


}