package com.kotlindiscord.kord.extensions.utils

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

/**
 * Class test for the extension of [Enum] class
 */
class EnumTest {

    /**
     * Fake class to simulate an enum
     */
    private enum class FakeEnum {
        I, AM, A, TEST
    }

    /**
     * Find the enum's value from his name in lower and upper case
     */
    @Test()
    fun `find enum by name with ignore case`() {
        FakeEnum.values().forEach {
            assertEquals(it, FakeEnum::class.findByName(it.name, true))
            assertEquals(it, FakeEnum::class.findByName(it.name.toLowerCase(), true))
        }
    }

    /**
     * Find the enum's value from his name without change the default case with the ignore case disabled
     */
    @Test()
    fun `find enum by name without ignore case`() {
        FakeEnum.values().forEach {
            assertEquals(it, FakeEnum::class.findByName(it.name, false))
        }
    }

    /**
     * No find an enum's value from his name when the case is modified with the ignore case disabled
     */
    @Test()
    fun `No find enum by name without ignore case`() {
        FakeEnum.values().forEach {
            assertNull(FakeEnum::class.findByName(it.name.toLowerCase(), false))
        }
    }

    /**
     * No find an enum's value with a name that doesn't exists in [FakeEnum]
     */
    @Test()
    fun `No find enum by name`() {
        val value = "other"
        assertNull(FakeEnum::class.findByName(value, true))
        assertNull(FakeEnum::class.findByName(value, false))
    }

}
