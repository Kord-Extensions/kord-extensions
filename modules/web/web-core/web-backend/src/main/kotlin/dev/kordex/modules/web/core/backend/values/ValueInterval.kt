/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

@file:Suppress("MagicNumber", "UnnecessaryParentheses")

package dev.kordex.modules.web.core.backend.values

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

public typealias ValueCheck = (now: LocalDateTime) -> Boolean

@Serializable
public sealed class ValueInterval(
	public val translationKey: String,

	@Transient
	public val check: ValueCheck = { false },
) {
	@Serializable
	public data object Second : ValueInterval("interval.second", {
		true
	})

	@Serializable
	public data object QuarterMinute : ValueInterval("interval.quarter-minute", { now ->
		now.second % 15 == 0
	})

	@Serializable
	public data object HalfMinute : ValueInterval("interval.half-minute", { now ->
		now.second % 30 == 0
	})

	@Serializable
	public data object Minute : ValueInterval("interval.minute", { now ->
		now.second == 0
	})

	@Serializable
	public data object QuarterHour : ValueInterval("interval.quarter-hour", { now ->
		now.second == 0 && (now.minute % 15 == 0)
	})

	@Serializable
	public data object HalfHour : ValueInterval("interval.half-hour", { now ->
		now.second == 0 && (now.minute % 30 == 0)
	})

	@Serializable
	public data object Hour : ValueInterval("interval.hour", { now ->
		now.second == 0 && (now.minute == 0)
	})

	@Serializable
	public data object QuarterDay : ValueInterval("interval.quarter-day", { now ->
		now.second == 0 && (now.hour % 6 == 0)
	})

	@Serializable
	public data object HalfDay : ValueInterval("interval.half-day", { now ->
		now.second == 0 && (now.hour % 12 == 0)
	})

	@Serializable
	public data object Day : ValueInterval("interval.day", { now ->
		now.second == 0 && (now.hour == 0)
	})

	public companion object {
		@Transient
		public val ALL: Set<ValueInterval> = setOf(
			Second,
			HalfMinute, Minute,
			QuarterHour, HalfHour, Hour,
			QuarterDay, HalfDay, Day,
		)
	}
}
