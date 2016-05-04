import common.util.address
import eventstore.client.EventStoreClient

fun main(args: Array<String>) {
    val esc = EventStoreClient(address("localhost", 9151))

    val es = esc.getStream("demo/test")

    es.observe(realtime = true)
            .subscribe (
                    { println(String(it)) },
                    { it.printStackTrace() }
            )


    println("enter to finish")
    readLine()
}