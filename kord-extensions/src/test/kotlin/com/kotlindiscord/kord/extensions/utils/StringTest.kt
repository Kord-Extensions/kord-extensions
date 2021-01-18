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
     * Check that `.startsWithVowel()` returns the correct result for values that start with vowels.
     */
    @Test
    fun `starts with a vowel`() {
        arrayOf("at", "et", "it", "ot", "ut").forEach {
            assertTrue(it.toUpperCase().startsWithVowel())
            assertTrue(it.startsWithVowel())
        }
    }

    /**
     * Check that `.startsWithVowel()` returns the correct result for values that **do not** start with vowels.
     */
    @Test
    fun `does not start with a vowel`() {
        arrayOf("yt", "ét", "àt", "ût", "wt", "/t").forEach {
            assertFalse(it.toUpperCase().startsWithVowel())
            assertFalse(it.startsWithVowel())
        }
    }

    /**
     * Check that `.parseBoolean()` correctly parses various strings into booleans.
     */
    @Test
    fun `parse strings to booleans`() {
        arrayOf("1", "y", "t", "yeah", "true").forEach {
            assertTrue(it.parseBoolean()!!)
            assertTrue(it.toUpperCase().parseBoolean()!!)
        }

        arrayOf("0", "n", "f", "no", "false").forEach {
            assertFalse(it.parseBoolean()!!)
            assertFalse(it.toUpperCase().parseBoolean()!!)
        }
    }

    /**
     * Check that `.parseBoolean()` returns `null` for invalid values.
     */
    @Test
    fun `parsing invalid strings to booleans returns null`() {
        arrayOf("2", "oui", "si").forEach {
            assertNull(it.parseBoolean())
        }
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
