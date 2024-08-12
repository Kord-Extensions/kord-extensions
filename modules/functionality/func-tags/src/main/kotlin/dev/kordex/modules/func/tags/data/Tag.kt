/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
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
