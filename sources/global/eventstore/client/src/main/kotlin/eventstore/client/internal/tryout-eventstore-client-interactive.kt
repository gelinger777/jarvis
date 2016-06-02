package eventstore.client.internal
import eventstore.client.EventStoreClient
import util.cpu
import java.util.concurrent.atomic.AtomicInteger

internal fun main(args: Array<String>) {
    val esc = EventStoreClient("localhost", 9151)

    val es = esc.getStream("demo/test")


    val count = AtomicInteger()


    es.read(start = 90)
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