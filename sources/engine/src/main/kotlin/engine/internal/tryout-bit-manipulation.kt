package engine.internal

internal fun main(args: Array<String>) {
    val a = 60    /* 60 = 0011 1100 */
    val b = 13    /* 13 = 0000 1101 */
    var c = 0

    c = a and b       /* 12 = 0000 1100 */
    println("a & b = " + c)

    c = a or b       /* 61 = 0011 1101 */
    println("a | b = " + c)

    c = a xor b       /* 49 = 0011 0001 */
    println("a ^ b = " + c)

    c = a.inv()          /*-61 = 1100 0011 */
    println("~a = " + c)

    c = a shl 2     /* 240 = 1111 0000 */
    println("a << 2 = " + c)

    c = a shr 2     /* 215 = 1111 */
    println("a >> 2  = " + c)

    c = a.ushr(2)     /* 215 = 0000 1111 */
    println("a >>> 2 = " + c)
}

