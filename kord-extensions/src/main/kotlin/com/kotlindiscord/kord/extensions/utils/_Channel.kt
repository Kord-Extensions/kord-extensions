package com.kotlindiscord.kord.extensions.utils

import dev.kord.core.behavior.channel.createWebhook
import dev.kord.core.entity.Webhook
import dev.kord.core.entity.channel.GuildMessageChannel
import dev.kord.core.firstOrNull
import dev.kord.rest.Image
import mu.KotlinLogging

private val LOG = KotlinLogging.logger {}

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
    channelObj: GuildMessageChannel,
    name: String,
    logoFormat: Image.Format = Image.Format.PNG,
    logo: (suspend () -> ByteArray)?
): Webhook {
    val webhook = channelObj.webhooks.firstOrNull { it.name == name }

    if (webhook != null) {
        return webhook
    }

    val guild = channelObj.guild.asGuild()

    LOG.info { "Creating webhook for channel: #${channelObj.name} (Guild: ${guild.name}" }

    return channelObj.createWebhook(name) {
        if (logo != null) {
            this.avatar = Image.raw(logo.invoke(), logoFormat)
        }
    }
}
