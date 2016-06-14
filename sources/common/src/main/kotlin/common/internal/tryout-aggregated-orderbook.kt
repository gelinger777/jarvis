package common.internal

import common.AggregatedOrderbook
import common.global.compact
import common.global.protoRandom
import util.app.log

internal fun main(args: Array<String>) {
    val book = AggregatedOrderbook()

    book.accept(protoRandom.randomOrder())

    book.stream().subscribe { it -> log.info { "1 : " + it.compact() } }

    book.accept(protoRandom.randomOrder())

    book.stream().subscribe { it -> log.info { "2 : " + it.compact() } }

    book.accept(protoRandom.randomOrder())

    book.stream().subscribe { it -> log.info { "3 : " + it.compact() } }

    book.accept(protoRandom.randomOrder())

}