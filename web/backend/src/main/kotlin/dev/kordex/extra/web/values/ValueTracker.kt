/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.kordex.extra.web.values

import com.kotlindiscord.kord.extensions.utils.collections.FixedLengthQueue

public abstract class ValueTracker<T : Any?> {
	@Suppress("MagicNumber")
	public open val maxValues: Int = 48
	public open val precision: ValueInterval = ValueInterval.HalfHour

	public abstract val identifier: String

	private val values: FixedLengthQueue<T> by lazy { FixedLengthQueue(maxValues) }

	public abstract suspend fun callback(): T

	public suspend fun update() {
		values.push(callback())
	}

	public fun getAll(): List<T> =
		values.getAll()
}
