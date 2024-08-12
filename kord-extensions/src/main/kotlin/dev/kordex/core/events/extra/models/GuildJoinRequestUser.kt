/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
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
