package eventstore.client.internal

import com.google.protobuf.ByteString
import proto.eventstore.ProtoES
import proto.eventstore.ProtoES.ReadReq
import proto.eventstore.ProtoES.WriteReq

internal fun writeReq(path: String, data: Collection<ByteString>): WriteReq {
    return WriteReq.newBuilder()
            .setPath(path)
            .addAllData(data)
            .build()
}

internal fun readReq(path: String, start: Long, end: Long): ReadReq{
    return ReadReq.newBuilder()
            .setPath(path)
            .setStart(start)
            .setEnd(end)
            .build()
}


internal fun streamReq(path: String): ProtoES.StreamReq {
    return ProtoES.StreamReq.newBuilder()
            .setPath(path)
            .build()
}