/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
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
