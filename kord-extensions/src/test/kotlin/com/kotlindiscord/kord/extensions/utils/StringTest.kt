package com.kotlindiscord.kord.extensions.utils

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

/**
 * Tests for [String] extension functions.
 */
class StringTest {

    /**
     * Check that `.toReaction()` for a unicode emoji transforms the string correctly.
     */
    @Test
    fun `unicode to reaction`() {
        val unicode = "❤"
        val reaction = unicode.toReaction()
        assertEquals(unicode, reaction.name)
    }

    /**
     * Check that `.splitOn()` correctly splits a string into a pair with the given separator.
     */
    @Test
    fun `splitting strings returns the correct pairs`() {
        assertEquals("Kord" to "-Ext", "Kord-Ext".splitOn { it == '-' })
        assertEquals("KordExt" to "", "KordExt".splitOn { it == '-' })
        assertEquals("Disc" to "ord bot", "Discord bot".splitOn { it == 'o' })

        assertEquals("Discord bot" to "", "Discord bot".splitOn { it == 'x' })
    }
}
