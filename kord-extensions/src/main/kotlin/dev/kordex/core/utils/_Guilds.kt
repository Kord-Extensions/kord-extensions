/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.utils

import dev.kord.common.entity.Permission
import dev.kord.common.entity.Permissions
import dev.kord.core.Kord
import dev.kord.core.behavior.GuildBehavior
import dev.kord.core.entity.Member
import dev.kord.core.entity.channel.GuildChannel

/**
 * Retrieves the member of the bot itself on this [GuildBehavior].
 *
 * @see Kord.selfId
 */
public suspend fun GuildBehavior.selfMember(): Member = getMember(kord.selfId)

/**
 * Checks whether the bot has at least [requiredPermissions] in this [GuildChannel].
 *
 * @see GuildBehavior.botHasPermissions
 */
public suspend fun GuildChannel.botHasPermissions(vararg requiredPermissions: Permission): Boolean =
	guild.botHasPermissions(this, Permissions(requiredPermissions.asIterable()))

/**
 * Checks whether the bot globally has at least [requiredPermissions] on this guild.
 *
 * @see GuildChannel.botHasPermissions
 */
public suspend fun GuildBehavior.botHasPermissions(vararg requiredPermissions: Permission): Boolean =
	botHasPermissions(null, Permissions(requiredPermissions.asIterable()))

private suspend fun GuildBehavior.botHasPermissions(channel: GuildChannel?, requiredPermissions: Permissions): Boolean {
	val selfMember = selfMember()
	val effectivePermissions =
		channel?.run { permissionsForMember(selfMember) }
			?: selfMember.getPermissions()

	return requiredPermissions in effectivePermissions
}
