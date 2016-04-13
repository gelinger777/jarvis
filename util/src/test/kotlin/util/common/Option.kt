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
        assertTrue { Option.of(1).isPresent() }
    }

    @Test
    fun testGetExisting() {
        assertEquals("val", Option.of("val").get())
    }

    @Test
    fun testFilter() {
        assertEquals(
                Option.empty(),
                Option.of("val").filter { it.isEmpty() }
        )
    }

    @Test
    fun testMap() {
        assertEquals(
                Option.of("VAL"),
                Option.of("val").map { it.toUpperCase() })
    }

    @Test
    fun testFlatMap() {
        assertEquals(
                Option.of("VAL"),
                Option.of("val").flatMap { Option.of(it.toUpperCase()) }
        )
    }

    @Test
    fun testIfNotPresentTakeValue() {
        assertEquals(
                Option.of("val"),
                Option.empty<String>().ifNotPresentTake("val")
        )
    }

    @Test
    fun testIfNotPresentTakeSupplier() {
        assertEquals(
                Option.of("val"),
                Option.empty<String>().ifNotPresentTakeCompute { "val" }
        )
    }

    @Test
    fun testTake() {
        assertEquals(
                Option.of(1),
                Option.of("val").take(1)
        )
        assertEquals(
                Option.of(1),
                Option.of("val").take(Option.of(1))
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
                Option.empty,
                Option.of("val").clear()
        )
    }

    @Test
    fun sample(){


    }

    private fun aToB(it: Int): String {
        throw UnsupportedOperationException("not implemented")
    }

}
