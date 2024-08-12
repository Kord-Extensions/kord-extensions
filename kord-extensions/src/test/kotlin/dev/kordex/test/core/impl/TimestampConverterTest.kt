/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.test.core.impl

import dev.kordex.core.commands.converters.impl.TimestampConverter
import dev.kordex.core.time.TimestampType
import kotlinx.datetime.Instant
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class TimestampConverterTest {

	@Test
	fun `timestamp without format`() {
		val timestamp = "<t:1420070400>" // 1st second of 2015
		val parsed = TimestampConverter.parseFromString(timestamp)!!
		assertEquals(Instant.fromEpochSeconds(1_420_070_400), parsed.instant)
		assertEquals(TimestampType.Default, parsed.format)
	}

	@Test
	fun `timestamp with format`() {
		val timestamp = "<t:1420070400:R>"
		val parsed = TimestampConverter.parseFromString(timestamp)!!
		assertEquals(Instant.fromEpochSeconds(1_420_070_400), parsed.instant)
		assertEquals(TimestampType.RelativeTime, parsed.format)
	}

	@Test
	fun `empty timestamp`() {
		val timestamp = "<t::>"
		val parsed = TimestampConverter.parseFromString(timestamp)
		assertNull(parsed)
	}

	@Test
	fun `timestamp with empty format`() {
		val timestamp = "<t:1420070400:>"
		val parsed = TimestampConverter.parseFromString(timestamp)
		assertNull(parsed)
	}
}
