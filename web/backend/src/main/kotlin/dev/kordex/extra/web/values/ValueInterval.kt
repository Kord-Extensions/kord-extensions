/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

@file:Suppress("UnnecessaryParentheses", "MagicNumber")

package dev.kordex.extra.web.values

import kotlinx.datetime.LocalDateTime

public typealias ValueCheck = (now: LocalDateTime) -> Boolean

public sealed class ValueInterval(public val translationKey: String, public val check: ValueCheck) {
	public data object Second : ValueInterval("interval.second", {
		true
	})

	public data object QuarterMinute : ValueInterval("interval.quarter-minute", { now ->
		now.second % 15 == 0
	})

	public data object HalfMinute : ValueInterval("interval.half-minute", { now ->
		now.second % 30 == 0
	})

	public data object Minute : ValueInterval("interval.minute", { now ->
		now.second == 0
	})

	public data object QuarterHour : ValueInterval("interval.quarter-hour", { now ->
		now.second == 0 && (now.minute % 15 == 0)
	})

	public data object HalfHour : ValueInterval("interval.half-hour", { now ->
		now.second == 0 && (now.minute % 30 == 0)
	})

	public data object Hour : ValueInterval("interval.hour", { now ->
		now.second == 0 && (now.minute == 0)
	})

	public data object QuarterDay : ValueInterval("interval.quarter-day", { now ->
		now.second == 0 && (now.hour % 6 == 0)
	})

	public data object HalfDay : ValueInterval("interval.half-day", { now ->
		now.second == 0 && (now.hour % 12 == 0)
	})

	public data object Day : ValueInterval("interval.day", { now ->
		now.second == 0 && (now.hour == 0)
	})

	public companion object {
		public val ALL: Set<ValueInterval> = setOf(
			Second,
			HalfMinute, Minute,
			QuarterHour, HalfHour, Hour,
			QuarterDay, HalfDay, Day,
		)
	}
}
