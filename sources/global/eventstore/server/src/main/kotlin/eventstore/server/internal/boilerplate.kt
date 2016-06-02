package eventstore.server.internal

import com.google.protobuf.ByteString
import proto.eventstore.ProtoES
import proto.eventstore.ProtoES.DataResp
import proto.eventstore.ProtoES.Event
import util.global.condition
import util.global.notNullOrEmpty

internal fun ProtoES.StreamReq.validate(): ProtoES.StreamReq {
    condition(notNullOrEmpty(this.path), "path must be provided")
    return this;
}

//internal fun ByteArray.toByteString(): ByteString {
//    return ByteString.copyFrom(this)
//}

internal fun Collection<Event>.toDataResponse(): DataResp {
    return DataResp.newBuilder()
            .setSuccess(true)
            .addAllEvents(this)
            .build()
}

internal fun Pair<Long, ByteArray>.asEvent(): Event {
    return Event.newBuilder()
            .setIndex(this.first)
            .setData(ByteString.copyFrom(this.second))
            .build();
}