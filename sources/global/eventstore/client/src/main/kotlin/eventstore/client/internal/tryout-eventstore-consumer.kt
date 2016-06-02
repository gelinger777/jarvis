package eventstore.client.internal
import eventstore.client.EventStoreClient

/**
 *
 */
internal fun main(args: Array<String>) {
    val esc = EventStoreClient(
            host = "localhost",
            port = 9151
    )

    val es = esc.getStream("demo/test")

    es.stream()
            .subscribe (
                    { println("${it.index} : ${String(it.data.toByteArray())}") },
                    { it.printStackTrace() },
                    { println("completed") }
            )


    println("enter to finish")
    readLine()
}