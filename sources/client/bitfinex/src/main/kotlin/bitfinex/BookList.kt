package bitfinex

import java.util.*

class BookList<K, V>(val comparator: Comparator<V>, val keyExtractor: (V) -> K) {

    val map = HashMap<K, Int>()
    val arr = ArrayList<V>()

    fun insert(value: V) {

    }

    fun remove(key: K) {

    }


}
