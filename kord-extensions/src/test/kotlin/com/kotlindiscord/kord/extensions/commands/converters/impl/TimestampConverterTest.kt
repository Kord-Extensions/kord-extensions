/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.commands.converters.impl

import com.kotlindiscord.kord.extensions.time.TimestampType
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
