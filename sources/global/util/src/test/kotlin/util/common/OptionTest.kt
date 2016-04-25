package util.common

import org.junit.Test
import util.Option
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class OptionTest {
    @Test(expected = IllegalStateException::class)
    fun testGetEmpty() {
        Option.empty.get()
    }

    @Test
    fun testIsPresent() {
        assertTrue { Option.Companion.of(1).isPresent() }
    }

    @Test
    fun testGetExisting() {
        assertEquals("val", Option.Companion.of("val").get())
    }

    @Test
    fun testFilter() {
        assertEquals(
                Option.Companion.empty(),
                Option.Companion.of("val").filter { it.isEmpty() }
        )
    }

    @Test
    fun testMap() {
        assertEquals(
                Option.Companion.of("VAL"),
                Option.Companion.of("val").map { it.toUpperCase() })
    }

    @Test
    fun testFlatMap() {
        assertEquals(
                Option.Companion.of("VAL"),
                Option.Companion.of("val").flatMap { Option.Companion.of(it.toUpperCase()) }
        )
    }

    @Test
    fun testIfNotPresentTakeValue() {
        assertEquals(
                Option.Companion.of("val"),
                Option.Companion.empty<String>().ifNotPresentTake("val")
        )
    }

    @Test
    fun testIfNotPresentTakeSupplier() {
        assertEquals(
                Option.Companion.of("val"),
                Option.Companion.empty<String>().ifNotPresentCompute { "val" }
        )
    }

    @Test
    fun testTake() {
        assertEquals(
                Option.Companion.of(1),
                Option.Companion.of("val").take(1)
        )
        assertEquals(
                Option.Companion.of(1),
                Option.Companion.of("val").take(Option.Companion.of(1))
        )
    }

    @Test
    fun testIfPresentAction() {
        var ifPresentExecuted = false
        var ifNotPresentExecuted = false

        Option.of("val")
                .ifPresent { ifPresentExecuted = true }
                .ifNotPresent { ifNotPresentExecuted = true }

        assertTrue { ifPresentExecuted }
        assertFalse { ifNotPresentExecuted }
    }

    @Test
    fun testIfNotPresentAction() {
        var ifPresentExecuted = false
        var ifNotPresentExecuted = false

        Option.empty
                .ifPresent { ifPresentExecuted = true }
                .ifNotPresent { ifNotPresentExecuted = true }

        assertFalse { ifPresentExecuted }
        assertTrue { ifNotPresentExecuted }
    }

    @Test
    fun testClear() {
        assertEquals(
                Option.Companion.empty,
                Option.Companion.of("val").clear()
        )
    }

    @Test
    fun sample(){


    }

    private fun aToB(it: Int): String {
        throw UnsupportedOperationException("not implemented")
    }

}
