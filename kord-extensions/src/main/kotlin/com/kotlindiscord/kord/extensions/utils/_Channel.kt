package com.kotlindiscord.kord.extensions.utils

import dev.kord.common.entity.Permissions
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.UserBehavior
import dev.kord.core.behavior.channel.createWebhook
import dev.kord.core.entity.Webhook
import dev.kord.core.entity.channel.GuildChannel
import dev.kord.core.entity.channel.MessageChannel
import dev.kord.core.entity.channel.TopGuildChannel
import dev.kord.core.entity.channel.TopGuildMessageChannel
import dev.kord.core.entity.channel.thread.ThreadChannel
import dev.kord.core.firstOrNull
import dev.kord.rest.Image
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mu.KotlinLogging
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

private val logger = KotlinLogging.logger {}

/**
 * The [Duration] it takes for [MessageChannel.type()] to timeout.
 */
@OptIn(ExperimentalTime::class)
@Suppress("MagicNumber")
public val CHANNEL_TYPING_TIMEOUT: Duration = Duration.seconds(8)

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
 * Calls [MessageChannel.type()] until [block] finishes running and returns the return value of [block].
 *
 * **Note**: Discord will only stop typing, if you send a message after finishing the task.
 *
 * Example usage:
 * ```kotlin
 * val response = channel.withTyping {
 *  heavyDataRequest()
 * }
 *
 * channel.createMessage(response)
 * ```
 */
@OptIn(ExperimentalTime::class, ExperimentalContracts::class)
public suspend fun <T> MessageChannel.withTyping(block: suspend () -> T): T {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    return coroutineScope {
        val typing = launch {
            type()
            delay(CHANNEL_TYPING_TIMEOUT)
        }

        block().also {
            typing.cancel()
        }
    }
}
