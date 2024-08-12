/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.events.extra.models

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

	@SerialName("form_responses")
	public val formResponses: List<GuildJoinRequestResponse>,

	@SerialName("actioned_by_user")
	public val actionedByUser: DiscordUser? = null,

	@SerialName("actioned_at")
	public val actionedAtSnowflake: Snowflake? = null,

	public val id: Snowflake,
	public val user: DiscordUser,

	// last_seen?
) {
	public val requestBypassed: Boolean by lazy {
		user.id == actionedByUser?.id
	}

	public val actionedAt: Instant? by lazy {
		actionedAtSnowflake?.timestamp
	}
}
