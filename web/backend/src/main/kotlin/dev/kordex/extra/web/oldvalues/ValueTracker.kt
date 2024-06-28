/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.kordex.extra.web.oldvalues

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.koin.KordExKoinComponent
import com.kotlindiscord.kord.extensions.utils.collections.FixedLengthQueue
import dev.kord.common.entity.Permissions
import dev.kord.core.Kord
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import org.koin.core.component.inject

@Serializable
public abstract class ValueTracker<V : Any?> : KordExKoinComponent {
	public val bot: ExtensibleBot by inject()
	public val kord: Kord by inject()

	public val settings: ValueTrackerSettings by lazy {
		ValueTrackerSettings(
			identifier = identifier,
			maxValues = maxValues,
			precision = precision,
			permissions = permissions
		)
	}

	public abstract val identifier: String

	@Suppress("MagicNumber")
	public open val maxValues: Int = 48
	public open val precision: ValueInterval = ValueInterval.HalfHour
	public open val permissions: Permissions? = null

	private val values: FixedLengthQueue<Value<V>> by lazy { FixedLengthQueue(maxValues) }

	public abstract suspend fun callback(): V

	public suspend fun update(now: Instant) {
		values.push(
			Value(now, callback())
		)
	}

	public fun getAll(): List<Value<V>> =
		values.getAll()
}
