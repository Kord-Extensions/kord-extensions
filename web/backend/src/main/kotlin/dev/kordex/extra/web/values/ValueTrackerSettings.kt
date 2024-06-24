/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.kordex.extra.web.values

import dev.kord.common.entity.Permissions
import kotlinx.serialization.Serializable

@Serializable
public data class ValueTrackerSettings(
	public val identifier: String,
	public val maxValues: Int,
	public val precision: ValueInterval,
	public val permissions: Permissions?,
)
