package extensions

import org.junit.Test
import kotlin.test.assertTrue


class ExtensionsTest {

    @Test(expected = IllegalStateException::class)
    fun testBothAbsent() {
        mutableMapOf(3 to "3").associateKeys(1, 2)
    }

    @Test(expected = IllegalStateException::class)
    fun testBothExist() {
        mutableMapOf(1 to "1", 2 to "2").associateKeys(1, 2)
    }

    @Test
    fun testAssociate() {
        val map = mutableMapOf(1 to "1")
        map.associateKeys(1, 2)
        assertTrue { map[1] === map[2] }
    }

    @Test
    fun testRemoveAssociates() {
        val value = "value"
        val map = mutableMapOf(1 to value, 2 to value)
        map.removeWithAssociations(1)
        assertTrue { map.isEmpty() }
    }
}