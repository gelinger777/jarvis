package global

import com.google.protobuf.GeneratedMessage
import com.google.protobuf.util.JsonFormat
import proto.Empty
import proto.ExecutionStatus
import proto.Pair
import util.repo
import java.util.regex.Pattern

// Empty

fun empty(): Empty {
    return Empty.getDefaultInstance()
}

fun success(): ExecutionStatus {
    return ExecutionStatus.newBuilder().setPlaceholder("success").build()
}


// Pair

/**
 * Convert to Pair instance.
 */
fun String.asPair(): Pair {

    val matcher = Pattern.compile("(.{3})[-|\\||/|-]?(.{3})").matcher(this)
    // BTCUSD
    // BTC|USD
    // BTC/USD
    // BTC-USD

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
    // btc-usd
    return "${this.base.symbol.toLowerCase()}-${this.quote.symbol.toLowerCase()}"
}

// json

/**
 * Convert to single line json representation
 */
fun GeneratedMessage.json(): String {
    return JsonFormat.printer().print(this).replace(Regex("[ |\\n]+"), " ")
}

