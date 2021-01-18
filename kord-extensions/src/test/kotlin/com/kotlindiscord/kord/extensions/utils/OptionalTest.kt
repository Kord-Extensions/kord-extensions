package com.kotlindiscord.kord.extensions.utils

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.util.*

/**
 * Tests for [Optional] extension functions.
 */
class OptionalTest {

    /**
     * Check that `.getOrNull()` returns the correct value.
     */
    @Test
    fun `get or null value`() {
        val value = 1
        val valueNull: Int? = null

        val opt = Optional.of(value)
        val result = opt.getOrNull()

        assertEquals(value, result)
        assertEquals(value, Optional.ofNullable(value).getOrNull())

        assertNull(Optional.ofNullable(valueNull).getOrNull())
    }
}
