import extensions.getOptional
import extensions.logger
import util.Option

object temp{
    val log by logger()
}

fun main(args: Array<String>) {
    val mutableMapOf = mutableMapOf<Any, Any>()

    val optional : Option<Any> = mutableMapOf.getOptional("key")

}