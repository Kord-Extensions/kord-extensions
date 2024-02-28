/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.events.extra.models

import dev.kord.common.entity.Snowflake
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class GuildJoinRequestUpdate(
	public val status: ApplicationStatus,
	public val request: GuildJoinRequest,

	@SerialName("guild_id")
	public val guildId: Snowflake,
)
