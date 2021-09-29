package com.kotlindiscord.kord.extensions.utils

import dev.kord.common.entity.Permission
import dev.kord.common.entity.Permissions
import dev.kord.core.Kord
import dev.kord.core.behavior.GuildBehavior
import dev.kord.core.entity.Member
import dev.kord.core.entity.channel.GuildChannel
import dev.kord.core.supplier.EntitySupplyStrategy

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
    val effectivePermissions =
        channel?.run { permissionsForMember(selfMember()) }
        // fast lane: this guild was fetched through rest
            ?: asGuild().permissions
            // slow lane: if guild was cached we fetch it to retrieve permissions field
            ?: withStrategy(EntitySupplyStrategy.rest).asGuild().permissions!!


    return requiredPermissions in effectivePermissions
}
