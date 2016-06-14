package util.global

/**
 * If expression is false throws IllegalStateException with specified message
 */
fun condition(condition: Boolean, message: String = "condition failure") {
    if (!condition) {
        throw IllegalStateException(message)
    }
}

/**
 * If expression is false throws WTF exception with specified message
 */
fun mandatoryCondition(condition: Boolean, message: String = "condition failure") {
    if (!condition) {
        wtf(message)
    }
}

fun notNull(arg: Any?): Boolean {
    return arg != null
}

fun notNullOrEmpty(string: String?): Boolean {
    return string != null && !string.isEmpty()
}

fun notNullOrEmpty(collection: Collection<Any>?): Boolean {
    return collection != null && !collection.isEmpty()
}

fun isNullOrEmpty(string: String?): Boolean {
    return string == null || string.length == 0
}

fun areSame(first: Any?, second: Any?): Boolean {
    return notNull(first) && notNull(second) && first === second
}
