package rpc.server

import extensions.stackTraceAsString
import io.grpc.stub.StreamObserver
import org.junit.Test
import proto.Pairs
import util.empty
import util.json

class CollectorServerTest {

    @Test
    fun accessibleMarketPairs() {
        CollectorServer().accessibleMarketPairs(empty(), object : StreamObserver<Pairs> {

            override fun onNext(pairs: Pairs) {
                println(pairs.json())
            }

            override fun onError(error: Throwable) {
                System.err.println(error.stackTraceAsString())
            }

            override fun onCompleted() {
                println("completed")
            }
        })
    }
}