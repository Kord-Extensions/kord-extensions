/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.utils

import dev.kord.common.entity.Permissions
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.UserBehavior
import dev.kord.core.behavior.channel.createWebhook
import dev.kord.core.entity.Message
import dev.kord.core.entity.Webhook
import dev.kord.core.entity.channel.GuildChannel
import dev.kord.core.entity.channel.TopGuildChannel
import dev.kord.core.entity.channel.TopGuildMessageChannel
import dev.kord.core.entity.channel.thread.ThreadChannel
import dev.kord.core.firstOrNull
import dev.kord.rest.Image
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * Ensure a webhook is created for the bot in a given channel, and return it.
 *
 * If a webhook already exists with the given name, it will be returned instead.
 *
 * @param channelObj Channel to create the webhook for.
 * @param name Name for the webhook
 * @param logoFormat Image.Format instance representing the format of the logo - defaults to PNG
 * @param logo Callable returning logo image data for the newly created webhook
 *
 * @return Webhook object for the newly created webhook, or the existing one if it's already there.
 */
public suspend fun ensureWebhook(
    channelObj: TopGuildMessageChannel,
    name: String,
    logoFormat: Image.Format = Image.Format.PNG,
    logo: (suspend () -> ByteArray)? = null
): Webhook {
    val webhook = channelObj.webhooks.firstOrNull { it.name == name }

    if (webhook != null) {
        return webhook
    }

    val guild = channelObj.guild.asGuild()

    logger.info { "Creating webhook for channel: #${channelObj.name} (Guild: ${guild.name}" }

    return channelObj.createWebhook(name) {
        if (logo != null) {
            this.avatar = Image.raw(logo.invoke(), logoFormat)
        }
    }
}

/**
 * Given a guild channel, attempt to calculate the effective permissions for the member corresponding with
 * the given ID, checking the parent channel if this one happens to be a thread.
 *
 * @param memberId Member ID to calculate for
 */
public suspend fun GuildChannel.permissionsForMember(memberId: Snowflake): Permissions = when (this) {
    is TopGuildChannel -> getEffectivePermissions(memberId)
    is ThreadChannel -> getParent().getEffectivePermissions(memberId)

    else -> error("Unsupported channel type for channel: $this")
}

/**
 * Given a guild channel, attempt to calculate the effective permissions for given user, checking the
 * parent channel if this one happens to be a thread.
 *
 * @param user User to calculate for
 */
public suspend fun GuildChannel.permissionsForMember(user: UserBehavior): Permissions =
    permissionsForMember(user.id)

/**
 * Convenience function that returns the thread's parent message, if it was created from one.
 *
 * If it wasn't, or the parent channel can't be found, this function returns `null`.
 */
public suspend fun ThreadChannel.getParentMessage(): Message? {
    val parentChannel = getParentOrNull() ?: return null

    return parentChannel.getMessageOrNull(this.id)
}
