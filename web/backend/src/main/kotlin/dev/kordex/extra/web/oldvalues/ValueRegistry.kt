/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.kordex.extra.web.oldvalues

import com.kotlindiscord.kord.extensions.utils.scheduling.Scheduler
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

public class ValueRegistry {
	private lateinit var scheduler: Scheduler
	private val trackers: MutableMap<String, ValueTracker<*>> = mutableMapOf()

	public suspend fun setup() {
		scheduler = Scheduler()

		scheduler.schedule(
			seconds = 1,
			startNow = true,
			name = "ValueRegistry repeating task",
			repeat = true
		) {
			val nowInstant = Clock.System.now()
			val now = nowInstant.toLocalDateTime(TimeZone.UTC)

			ValueInterval.ALL.forEach { interval ->
				if (interval.check(now)) {
					scheduler.launch {
						trackers.values
							.filter { it.precision == interval }
							.forEach { it.update(nowInstant) }
					}
				}
			}
		}
	}

	public fun shutdown() {
		trackers.clear()
		scheduler.shutdown()
	}

	public fun register(tracker: ValueTracker<*>) {
		if (trackers.contains(tracker.identifier)) {
			error("Already tracking value with identifier: ${tracker.identifier}")
		}

		trackers[tracker.identifier] = tracker
	}

	public fun get(identifier: String): ValueTracker<*>? =
		trackers[identifier]

	public fun remove(tracker: ValueTracker<*>): ValueTracker<*>? =
		remove(tracker.identifier)

	public fun remove(identifier: String): ValueTracker<*>? =
		trackers.remove(identifier)

	public fun getAll(): List<ValueTracker<*>> =
		trackers.values.toList()

	public fun getAllValues(): Map<String, List<Value<*>>> =
		trackers.mapValues { (_, value) -> value.getAll() }
}
