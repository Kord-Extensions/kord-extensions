package com.kotlindiscord.kord.extensions.utils

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.util.*

/**
 * Class test for the extension of [Optional] class
 */
class OptionalTest {

    /**
     * Test the retrieve of value when the optional value can be null or not
     */
    @Test
    fun `get or null value`() {
        val value = 1
        val opt = Optional.of(value)
        val result = opt.getOrNull()
        assertEquals(value, result)
        
        val valueNull: Int? = null
        assertNull(Optional.ofNullable(valueNull).getOrNull())
        assertEquals(value, Optional.ofNullable(value).getOrNull())
    }
}
