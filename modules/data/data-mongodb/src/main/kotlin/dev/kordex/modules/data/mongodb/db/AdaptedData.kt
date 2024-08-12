/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.modules.data.mongodb.db

import dev.kord.common.entity.Snowflake
import dev.kordex.core.storage.StorageType
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
@Suppress("ConstructorParameterNaming", "DataClassShouldBeImmutable")
internal data class AdaptedData(
	@Contextual
	override val _id: String,

	val identifier: String,

	val type: StorageType? = null,

	val channel: Snowflake? = null,
	val guild: Snowflake? = null,
	val message: Snowflake? = null,
	val user: Snowflake? = null,

	var data: String,
) : Entity<String>
