/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.modules.extra.pluralkit.events

import com.kotlindiscord.kord.extensions.events.KordExEvent
import com.kotlindiscord.kord.extensions.modules.extra.pluralkit.api.PKMessage
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.channel.MessageChannelBehavior
import dev.kord.core.entity.Guild
import dev.kord.core.entity.Member
import dev.kord.core.entity.Message
import dev.kord.core.entity.Strategizable
import dev.kord.core.entity.channel.MessageChannel
import dev.kord.core.event.message.MessageDeleteEvent
import dev.kord.core.supplier.EntitySupplier
import dev.kord.core.supplier.EntitySupplyStrategy
import dev.kord.core.supplier.getChannelOf
import dev.kord.core.supplier.getChannelOfOrNull

/**
 * A [MessageDeleteEvent] wrapper that fires with extra context that may be supplied by PluralKit. Subtypes are
 * available if you need to be more specific about whether the message was proxied or not.
 *
 * @property event The original event that triggered this one.
 * @property channelId The ID for the channel this message was sent to.
 * @property message The message object in question, which may be a webhook message if proxied.
 * @property guildId The ID for the guild that this message was sent on, if any.
 * @property author The original account that sent the message, even if it was proxied.
 * @property repliedToMessage The original message that was replied to, even if this one was proxied.
 */
abstract class PKMessageDeleteEvent(
    open val event: MessageDeleteEvent,
    open val channelId: Snowflake,
    open val message: Message?,
    open val guildId: Snowflake?,
    open val author: Member?,
    open val repliedToMessage: Message?,
    override val shard: Int,
    override val supplier: EntitySupplier = event.kord.defaultSupplier,
) : KordExEvent, Strategizable {
    /** @suppress Forwards to [repliedToMessage], **/
    val referencedMessage get() = repliedToMessage

    /** Channel behaviour representing the channel the message was sent to. **/
    val channel: MessageChannelBehavior
        get() = MessageChannelBehavior(channelId, kord)

    /** Attempt to retrieve the channel this message was sent to, throwing if it can't be found. **/
    suspend inline fun <reified T : MessageChannel> getChannel(): T =
        supplier.getChannelOf<T>(channelId)

    /** Attempt to retrieve the channel this message was sent to, returning null if it can't be found. **/
    suspend inline fun <reified T : MessageChannel> getChannelOrNull(): T? =
        supplier.getChannelOfOrNull(channelId)

    /** Attempt to retrieve the guild this message was sent to, returning null if it can't be found. **/
    suspend fun getGuild(): Guild? =
        guildId?.let { supplier.getGuildOrNull(it) }
}

/**
 * A [MessageDeleteEvent] wrapper that represents a message that was proxied by PluralKit.
 *
 * @property pkMessage The PluralKit message object with metadata about the proxied message.
 */
class ProxiedMessageDeleteEvent(
    event: MessageDeleteEvent,
    channelId: Snowflake,
    message: Message?,
    guildId: Snowflake?,
    override val author: Member,
    repliedToMessage: Message?,
    shard: Int,
    val pkMessage: PKMessage,
    supplier: EntitySupplier = event.kord.defaultSupplier,
) : PKMessageDeleteEvent(event, channelId, message, guildId, author, repliedToMessage, shard, supplier) {
    override fun withStrategy(strategy: EntitySupplyStrategy<*>): ProxiedMessageDeleteEvent {
        val strategizedEvent = event.withStrategy(strategy)

        return ProxiedMessageDeleteEvent(
            strategizedEvent,
            channelId,
            strategizedEvent.message,
            guildId,
            author,
            repliedToMessage,
            shard,
            pkMessage,
            strategy.supply(kord)
        )
    }

    override fun toString(): String =
        "ProxiedMessageDeleteEvent(" +
            "event=$event, " +
            "channelId=$channelId, " +
            "message=$message, " +
            "guildId=$guildId, " +
            "author=$author, " +
            "shard=$shard, " +
            "repliedToMessage=$repliedToMessage, " +
            "supplier=$supplier" +
            ")"
}

/**
 * A [MessageDeleteEvent] wrapper that represents a message that was **not** proxied by PluralKit. You may also
 * receive this when a message was proxied by PluralKit, but was not cached for some reason.
 */
class UnProxiedMessageDeleteEvent(
    event: MessageDeleteEvent,
    channelId: Snowflake,
    message: Message?,
    guildId: Snowflake?,
    author: Member?,
    repliedToMessage: Message?,
    shard: Int,
    supplier: EntitySupplier = event.kord.defaultSupplier,
) : PKMessageDeleteEvent(event, channelId, message, guildId, author, repliedToMessage, shard, supplier) {
    override fun withStrategy(strategy: EntitySupplyStrategy<*>): UnProxiedMessageDeleteEvent {
        val strategizedEvent = event.withStrategy(strategy)

        return UnProxiedMessageDeleteEvent(
            strategizedEvent,
            channelId,
            strategizedEvent.message,
            guildId,
            author,
            repliedToMessage,
            shard,
            strategy.supply(kord)
        )
    }

    override fun toString(): String =
        "UnProxiedMessageDeleteEvent(" +
            "event=$event, " +
            "channelId=$channelId, " +
            "message=$message, " +
            "guildId=$guildId, " +
            "author=$author, " +
            "repliedToMessage=$repliedToMessage, " +
            "shard=$shard, " +
            "supplier=$supplier" +
            ")"
}

internal suspend fun MessageDeleteEvent.proxied(p: PKMessage, referencedMessage: Message?): ProxiedMessageDeleteEvent {
    val member = getGuild()!!.getMemberOrNull(p.sender)!!

    return ProxiedMessageDeleteEvent(
        this,
        channelId,
        message,
        guildId,
        member,
        referencedMessage,
        shard,
        p,
    )
}

internal suspend fun MessageDeleteEvent.unproxied(): UnProxiedMessageDeleteEvent =
    UnProxiedMessageDeleteEvent(
        this,
        channelId,
        message,
        guildId,
        message?.getAuthorAsMember(),
        message?.referencedMessage,
        shard
    )
