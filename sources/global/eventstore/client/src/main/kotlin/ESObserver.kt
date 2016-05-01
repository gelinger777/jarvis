import eventstore.client.EventStoreClient

fun main(args: Array<String>) {
    val esc = EventStoreClient("localhost", 9151)

    val es = esc.getStream("demo/test")

    es.observe(realtime = true)
            .subscribe (
                    { println(String(it)) },
                    { it.printStackTrace()}
            )


    println("enter to finish")
    readLine()
}