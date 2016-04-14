package util

import proto.*
import java.util.regex.Pattern

// Empty

fun empty(): Empty {
    return Empty.getDefaultInstance()
}


// Currency

/**
 * Create or get existing Currency.
 */
fun Currency.from(symbol: String): Currency {
    return repo.currency(symbol)
}

/**
 * JSON representation of a pair.
 */
fun Currency.json(): String {
    return "{${this.symbol}}"
}

// Pair

/**
 * JSON representation of a pair.
 */
fun Pair.json(): String {
    return "{${this.base.symbol} | ${this.quote.symbol}}"
}

/**
 * Convert to Pair instance.
 */
fun String.asPair(): Pair {
    val matcher = Pattern.compile("(.{3})([-|\\||/])?(.{3})").matcher(this)
    matcher.find()

    return repo.pair(
            base = matcher.group(1),
            quote = matcher.group(3)
    )
}

/**
 * Path friendly identifier for representing a pair.
 */
fun Pair.folderName(): String {
    return "${this.base.symbol.toLowerCase()}-${this.quote.symbol.toLowerCase()}"
}

// Order

fun Order.json(): String {
    return "{$id | $side | $price | $volume}}"
}


fun ByteArray.toOrder(): Order {
    return Order.parseFrom(this)
}

// Trade

/**
 * JSON representation of a pair.
 */
fun Trade.json(): String {
    //    val time = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).format(Instant.ofEpochMilli(this.time))
    return "{$price | $volume}"
}