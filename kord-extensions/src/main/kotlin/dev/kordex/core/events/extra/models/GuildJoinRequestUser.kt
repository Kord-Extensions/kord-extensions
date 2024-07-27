/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.kordex.core.events.extra.models

import dev.kord.common.entity.Snowflake
import dev.kord.common.entity.UserFlags
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class GuildJoinRequestUser(
	@SerialName("public_flags")
	public val flags: UserFlags,

	@SerialName("display_name")
	public val displayName: String?,

	public val username: String,
	public val id: Snowflake,
	public val discriminator: Int,
	public val avatar: String,

	// avatar_decoration?
)
