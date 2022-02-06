/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.utils

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode

/**
 * Tests for [String] extension functions.
 */
class StringTest {

    /**
     * Check that `.toReaction()` for a unicode emoji transforms the string correctly.
     */
    @Test
    @Execution(ExecutionMode.CONCURRENT)
    fun `unicode to reaction`() {
        val unicode = "❤"
        val reaction = unicode.toReaction()
        assertEquals(unicode, reaction.name)
    }

    /**
     * Check that `.splitOn()` correctly splits a string into a pair with the given separator.
     */
    @Test
    @Execution(ExecutionMode.CONCURRENT)
    fun `splitting strings returns the correct pairs`() {
        assertEquals("Kord" to "-Ext", "Kord-Ext".splitOn { it == '-' })
        assertEquals("KordExt" to "", "KordExt".splitOn { it == '-' })
        assertEquals("Disc" to "ord bot", "Discord bot".splitOn { it == 'o' })

        assertEquals("Discord bot" to "", "Discord bot".splitOn { it == 'x' })
    }
}
