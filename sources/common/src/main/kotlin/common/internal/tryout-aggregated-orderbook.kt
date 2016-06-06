package common.internal

import common.AggregatedOrderbook
import common.global.compact
import common.global.protoRandom

internal fun main(args: Array<String>) {
    val book = AggregatedOrderbook()

    book.accept(protoRandom.randomOrder())

    book.stream().subscribe { it -> println("1 : " + it.compact()) }

    book.accept(protoRandom.randomOrder())

    book.stream().subscribe { it -> println("2 : " + it.compact()) }

    book.accept(protoRandom.randomOrder())

    book.stream().subscribe { it -> println("3 : " + it.compact()) }

    book.accept(protoRandom.randomOrder())

}