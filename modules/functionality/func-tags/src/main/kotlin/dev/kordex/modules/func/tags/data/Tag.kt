/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.kordex.modules.func.tags.data

import dev.kord.common.Color
import dev.kord.common.entity.Snowflake
import kotlinx.serialization.Serializable

/**
 * Data class representing a single tag. Serializable, for flexible storage.
 */
@Serializable
data class Tag(
	val category: String,
	val description: String,
	val key: String,
	val title: String,

	val color: Color? = null,
	val guildId: Snowflake? = null,
	val image: String? = null,
)
