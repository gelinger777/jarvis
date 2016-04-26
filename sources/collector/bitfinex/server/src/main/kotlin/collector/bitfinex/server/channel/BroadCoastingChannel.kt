package collector.bitfinex.server.channel

import com.google.gson.JsonArray
import io.grpc.stub.StreamObserver

internal abstract class BroadCoastingChannel<T>(
        var name:String,
        val observers:MutableSet<StreamObserver<T>> = mutableSetOf()
){
    fun addObserver(observer: StreamObserver<T>) {
        observers.add(observer)
    }

    fun complete() {
        observers.forEach { it.onCompleted() }
    }

    fun removeObserver(observer: StreamObserver<T>) {
        observers.remove(observer);
        observer.onCompleted()
    }

    abstract fun parse(array: JsonArray)
}
