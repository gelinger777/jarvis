package util

import proto.Messages
import util.repo
import java.util.regex.Pattern



// Currency

/**
 * Create or get existing Currency.
 */
fun Messages.Currency.from(symbol: String): Messages.Currency {
    return repo.currency(symbol)
}

/**
 * JSON representation of a pair.
 */
fun Messages.Currency.json(): String {
    return "{${this.symbol}}"
}

// Pair

/**
 * JSON representation of a pair.
 */
fun Messages.Pair.json(): String {
    return "{${this.base.symbol} | ${this.quote.symbol}}"
}

/**
 * Convert to Pair instance.
 */
fun String.asPair(): Messages.Pair {
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
fun Messages.Pair.folderName(): String {
    return "${this.base.symbol.toLowerCase()}-${this.quote.symbol.toLowerCase()}"
}

// Order

fun Messages.Order.json() : String{
    return "{$id | $side | $price | $volume}}"
}


fun ByteArray.toOrder(): Messages.Order {
    return Messages.Order.parseFrom(this)
}

// Trade

/**
 * JSON representation of a pair.
 */
fun Messages.Trade.json(): String {
//    val time = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).format(Instant.ofEpochMilli(this.time))
    return "{$price | $volume}"
}