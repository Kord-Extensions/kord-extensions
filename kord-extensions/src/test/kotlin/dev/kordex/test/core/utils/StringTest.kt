/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.test.core.utils

import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.ReactionEmoji
import dev.kordex.core.utils.splitOn
import dev.kordex.core.utils.toReaction
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import kotlin.test.assertNotNull

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
	 * Check that `.toReaction()` for custom emojis transforms the string correctly.
	 */
	@Suppress("UnderscoresInNumericLiterals", "MagicNumber")
	@Test
	@Execution(ExecutionMode.CONCURRENT)
	fun `custom to reaction`() {
		val staticString = "<:cozy:859058952393457694>"
		val staticReaction = staticString.toReaction() as? ReactionEmoji.Custom

		assertNotNull(staticReaction, "Returned ReactionEmoji object must be a custom emoji")

		assertEquals(staticReaction.name, "cozy")
		assertEquals(staticReaction.id, Snowflake(859058952393457694))
		assertEquals(staticReaction.isAnimated, false)

		val animatedString = "<a:ablobzerogravity:614485981021601812>"
		val animatedReaction = animatedString.toReaction() as? ReactionEmoji.Custom

		assertNotNull(animatedReaction, "Returned ReactionEmoji object must be a custom emoji")

		assertEquals(animatedReaction.name, "ablobzerogravity")
		assertEquals(animatedReaction.id, Snowflake(614485981021601812))
		assertEquals(animatedReaction.isAnimated, true)
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
