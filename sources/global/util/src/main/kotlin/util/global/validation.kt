package util.global

/**
 * If expression is false throws IllegalStateException
 */
fun condition(condition: Boolean) {
    if (!condition) {
        throw IllegalStateException()
    }
}

/**
 * If expression is false throws IllegalStateException with specified message
 */
fun condition(condition: Boolean, message: String) {
    if (!condition) {
        throw IllegalStateException(message)
    }
}

///**
// * If expression is false throws IllegalStateException with specified message
// */
//fun condition(condition: Boolean, message: String, vararg args: Any) {
//    condition(condition, String.format(message, *args))
//}

//// null checking
//fun isNull(arg: Any?): Boolean {
//    return arg == null
//}
//
fun notNull(arg: Any?): Boolean {
    return arg != null
}

//
//// vararg null checking (all, any)
//
//fun anyNull(vararg objects: Any): Boolean {
//    condition(allNotNullOrEmpty(*objects), "at least one argument is expected")
//    for (argument in objects) {
//        if (argument == null) {
//            return true
//        }
//    }
//    return false
//}
//
//fun allNull(vararg objects: Any): Boolean {
//    condition(allNotNullOrEmpty(*objects), "at least one argument is expected")
//    for (argument in objects) {
//        if (argument == null) {
//            return true
//        }
//    }
//    return false
//}
//
//fun allNotNull(vararg objects: Any): Boolean {
//    condition(allNotNullOrEmpty(*objects), "at least one argument is expected")
//    for (argument in objects) {
//        if (argument == null) {
//            return false
//        }
//    }
//    return true
//}
//
//// equality
//
///**
// * @return true if all objects are equal, false if any of objects is not equal or is null
// */
//fun areEqual(vararg objects: Any): Boolean {
//    condition(allNotNullOrEmpty(*objects), "at least one argument is expected")
//
//    if (anyNull(*objects)) {
//        return false
//    }
//
//    var previous = objects[0]
//    for (i in 1..objects.size - 1) {
//        if (objects[i] != previous) {
//            return false
//        } else {
//            previous = objects[i]
//        }
//    }
//    return true
//}
//
//fun notEqual(vararg objects: Any): Boolean {
//    if (anyNull(*objects)) {
//        return false
//    }
//    if (objects.size == 1) {
//        return true
//    }
//
//    var previous = objects[0]
//    for (i in 1..objects.size - 1) {
//        if (objects[i] != previous) {
//            return true
//        } else {
//            previous = objects[i]
//        }
//    }
//    return false
//}
//
//// not null or empty string/collection
//
fun notNullOrEmpty(string: String?): Boolean {
    return string != null && !string.isEmpty()
}

fun notNullOrEmpty(collection: Collection<Any>?): Boolean {
    return collection != null && !collection.isEmpty()
}


fun isNullOrEmpty(string: String?): Boolean {
    return string == null || string.length == 0
}
//
//fun isNullOrEmpty(vararg objects: Any): Boolean {
//    return objects == null || objects.size == 0
//}
//
//fun isNullOrEmpty(collection: Collection<Any>?): Boolean {
//    return collection == null || collection.isEmpty()
//}
//
//// all not null or empty
//
//fun allNotNullOrEmpty(vararg strings: String): Boolean {
//    condition(notNullOrEmpty(strings), "at least one argument is expected")
//
//    for (string in strings) {
//        if (isNullOrEmpty(string)) {
//            return false
//        }
//    }
//    return true
//}
//
//fun notNullOrEmpty(objects: Array<Any>?): Boolean {
//    return objects != null && objects.size > 0
//}
//
//fun allNotNullOrEmpty(vararg collections: Collection<Any>): Boolean {
//    condition(notNullOrEmpty(collections), "at least one argument is expected")
//
//    for (collection in collections) {
//        if (isNullOrEmpty(collection)) {
//            return false
//        }
//    }
//    return true
//}
//
//fun allNotNullOrEmpty(strings: Collection<String>?): Boolean {
//    if (strings == null || strings.size == 0) {
//        return false
//    }
//    for (str in strings) {
//        if (isNullOrEmpty(str)) {
//            return false
//        }
//    }
//    return true
//}
//
//// uppercase/lowercase
//
//fun isUppercase(string: String?): Boolean {
//    return string != null && string == string.toUpperCase()
//}
//
//fun areUpperCase(vararg strings: String): Boolean {
//    for (string in strings) {
//        if (!isUppercase(string)) {
//            return false
//        }
//    }
//    return true
//}
//
//fun isLowercase(string: String?): Boolean {
//    return string != null && string == string.toLowerCase()
//}
//
//// compare ignore order
//
//fun compareIgnoreOrder(expected: Array<Any>, actual: Array<Any>): Boolean {
//    return asList(*expected).containsAll(asList(*actual))
//}
//
//fun notNullOrEmpty(data: ByteArray?): Boolean {
//    return data != null && data.size > 0
//}
