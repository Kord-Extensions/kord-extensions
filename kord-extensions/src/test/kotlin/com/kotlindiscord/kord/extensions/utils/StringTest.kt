package com.kotlindiscord.kord.extensions.utils

import dev.kord.core.entity.ReactionEmoji
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

/**
 * Class test for the extension of [String] class
 */
class StringTest {

    /**
     * Transform a [String] to a [ReactionEmoji] and compare the unicode value
     */
    @Test
    fun `unicode to reaction`() {
        val unicode = "❤"
        val reaction = unicode.toReaction()
        assertEquals(unicode, reaction.name)
    }

    /**
     * Test if the words starting by a, e, i, o, u are considered as beginning with a vowel
     */
    @Test
    fun `start with vowel`() {
        arrayOf("at", "et", "it", "ot", "ut").forEach {
            assertTrue(it.startsWithVowel())
        }
    }

    /**
     * Test if the words starting by y, accent, random char, are don't considered as beginning with a vowel
     */
    @Test
    fun `no start with vowel`() {
        arrayOf("at", "et", "it", "ot", "ut").forEach {
            assertFalse(it.toUpperCase().startsWithVowel())
        }

        arrayOf("yt", "ét", "àt", "ût", "wt", "/t").forEach {
            assertFalse(it.toUpperCase().startsWithVowel())
            assertFalse(it.startsWithVowel())
        }
    }

    /**
     * Test the parsing of [String] to [Boolean] without error
     */
    @Test
    fun `parse string to boolean with several format`() {
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
     * Test the parsing of [String] to [Boolean] with error and get null value
     */
    @Test
    fun `parse string to boolean with several format and get null value`() {
        arrayOf("2", "oui", "si").forEach {
            assertNull(it.parseBoolean())
        }
    }

    /**
     * Split a string in two parts with the index if a char
     */
    @Test
    fun `split string based on the index of a char`() {
        assertEquals("Kord" to "-Ext", "Kord-Ext".splitOn { it == '-' })
        assertEquals("KordExt" to "", "KordExt".splitOn { it == '-' })
        assertEquals("Disc" to "ord bot", "Discord bot".splitOn { it == 'o' })
    }
}
