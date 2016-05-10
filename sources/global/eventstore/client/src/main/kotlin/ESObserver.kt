import common.util.address
import eventstore.client.EventStoreClient
import util.cpu

fun main(args: Array<String>) {
    val esc = EventStoreClient(address("localhost", 9151))

    val es = esc.getStream("demo/test")

    es.read()
            .observeOn(cpu.schedulers.io)
            .subscribe (
                    { println("${it.index} : ${String(it.data.toByteArray())}") },
                    { it.printStackTrace() },
                    { println("completed") }
            )


    println("enter to finish")
    readLine()
}