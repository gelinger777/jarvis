
import common.global.address
import eventstore.client.EventStoreClient
import util.cpu

@Volatile var flag = true

fun main(args: Array<String>) {
    val esc = EventStoreClient(address("localhost", 9151))

    val es = esc.getStream("demo/test")


    cpu.executors.io.submit {
        var counter = 0
        while (flag) {
            es.write("message ${counter++}".toByteArray())
        }
    }

    println("enter to finish")
    readLine()
    flag = false
}
