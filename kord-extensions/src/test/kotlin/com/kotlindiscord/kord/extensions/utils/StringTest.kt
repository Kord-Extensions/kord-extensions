package com.kotlindiscord.kord.extensions.utils

import com.kotlindiscord.kord.extensions.ParseException
import dev.kord.core.entity.ReactionEmoji
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import kotlin.jvm.Throws

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
            assertTrue(it.toUpperCase().startsWithVowel())
            assertTrue(it.startsWithVowel())
        }
    }

    /**
     * Test if the words starting by y, accent, random char, are don't considered as beginning with a vowel
     */
    @Test
    fun `no start with vowel`() {
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
        arrayOf("1", "y", "yes", "t", "true").forEach {
            assertTrue(it.toUpperCase().parseBoolean())
            assertTrue(it.parseBoolean())
        }

        arrayOf("0", "n", "no", "f", "false").forEach {
            assertFalse(it.toUpperCase().parseBoolean())
            assertFalse(it.parseBoolean())
        }
    }

    /**
     * Test the parsing of [String] to [Boolean] with error and get null value
     */
    @Test
    fun `parse string to boolean with several format and get null value`() {
        arrayOf("2", "yeah", "oui", "si", "ye").forEach {
            assertNull(it.parseBooleanOrNull())
        }
    }

    /**
     * Test the parsing of [String] to [Boolean] with error and throw an exception
     */
    @Test()
    fun `parse string to boolean with several format and get exception`() {
        arrayOf("2", "yeah", "oui", "si", "ye").forEach {
            assertThrows(ParseException::class.java) {  it.parseBoolean() }
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
