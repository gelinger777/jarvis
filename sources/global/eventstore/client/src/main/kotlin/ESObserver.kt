
import common.util.address
import eventstore.client.EventStoreClient

fun main(args: Array<String>) {
    val esc = EventStoreClient(address("localhost", 9151))

    val es = esc.getStream("demo/test")

    es.stream()
            .subscribe (
                    { println("${it.index} : ${String(it.data.toByteArray())}") },
                    { it.printStackTrace() }
            )


    println("enter to finish")
    readLine()
}