/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.events.extra.models

import dev.kord.common.entity.DiscordUser
import dev.kord.common.entity.Snowflake
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class GuildJoinRequest(
    @SerialName("user_id")
    public val userId: Snowflake,

    @SerialName("rejection_reason")
    public val rejectionReason: String?,

    @SerialName("guild_id")
    public val guildId: Snowflake,

    @SerialName("created_at")
    public val createdAt: Instant,

    @SerialName("application_status")
    public val status: ApplicationStatus,

    public val id: Snowflake,
    public val formResponses: List<GuildJoinRequestResponse>,
    public val user: DiscordUser,

    @SerialName("actioned_by_user")
    public val actionedByUser: DiscordUser?,

    @SerialName("actioned_at")
    public val actionedAtSnowflake: Snowflake,

    // last_seen?
) {
    public val requestBypassed: Boolean by lazy {
        user.id == actionedByUser?.id
    }

    public val actionedAt: Instant by lazy {
        actionedAtSnowflake.timestamp
    }
}
