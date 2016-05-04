
import common.util.address
import eventstore.client.EventStoreClient
import util.cpu
import java.util.concurrent.atomic.AtomicInteger

fun main(args: Array<String>) {
    val esc = EventStoreClient(address("localhost", 9151))

    val es = esc.getStream("demo/test")


    val count = AtomicInteger()


    es.observe(start = 90, realtime = true)
            .subscribe ({
                println("${Thread.currentThread().name} : ${count.incrementAndGet()}")
            })

    for (i in 1..100) {
        cpu.sleep(1000)
        es.write("data".toByteArray())
    }

    println("enter to finish")
    readLine()
}