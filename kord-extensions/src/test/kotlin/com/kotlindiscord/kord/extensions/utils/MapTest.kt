/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.utils

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import kotlin.test.assertNull

/**
 * Tests for [Map] extension functions.
 */
class MapTest {
    /**
     * Check that the typed map getters return the values expected of them.
     */
    @Test
    @Execution(ExecutionMode.CONCURRENT)
    fun `check typed getters`() {
        val map: MutableStringKeyedMap<Any> = mutableMapOf(
            "string" to "value",
            "number" to 1
        )

        assertEquals(1, map.getOf("number"))
        assertEquals("value", map.getOf("string"))

        assertNull(map.getOfOrNull("missing"))
        assertEquals(2, map.getOrDefault("missing", 2))

        assertThrows<ClassCastException> { map.getOf<String>("number") }
        assertThrows<IllegalArgumentException> { map.getOf<Int>("missing") }

        assertEquals("two", map.getOfOrDefault("two", "two", true))
        assertEquals("two", map.getOf("two"))
        assertThrows<ClassCastException> { map.getOf<Int>("two") }

        assertEquals(2, map.getOfOrDefault("two", 2, true))
        assertEquals(2, map.getOf("two"))
        assertThrows<ClassCastException> { map.getOf<String>("two") }
    }
}
