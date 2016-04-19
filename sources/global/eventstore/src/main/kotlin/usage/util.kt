package usage

import java.nio.ByteBuffer


internal fun toByteArray(value: Int): ByteArray {
    return ByteBuffer.allocate(4).putInt(value).array()
}

internal fun fromByteArray(bytes: ByteArray): Int {
    return ByteBuffer.wrap(bytes).int
}