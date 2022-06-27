/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.modules.extra.pluralkit.events

import com.kotlindiscord.kord.extensions.events.KordExEvent
import com.kotlindiscord.kord.extensions.modules.extra.pluralkit.api.PKMessage
import dev.kord.common.entity.DiscordPartialMessage
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.MessageBehavior
import dev.kord.core.behavior.channel.MessageChannelBehavior
import dev.kord.core.entity.Member
import dev.kord.core.entity.Message
import dev.kord.core.entity.Strategizable
import dev.kord.core.entity.channel.GuildChannel
import dev.kord.core.event.message.MessageUpdateEvent
import dev.kord.core.supplier.EntitySupplier
import dev.kord.core.supplier.EntitySupplyStrategy

/**
 * A [MessageUpdateEvent] wrapper that fires with extra context that may be supplied by PluralKit. Subtypes are
 * available if you need to be more specific about whether the message was proxied or not.
 *
 * @property event The original event that triggered this one.
 * @property channelId The ID for the channel this message was sent to.
 * @property messageId The ID of the message that was updated.
 * @property old The old message object, if it was cached..
 * @property new A partial object representing the updated message..
 * @property author The original account that sent the message, even if it was proxied.
 * @property repliedToMessage The original message that was replied to, even if this one was proxied.
 */
abstract class PKMessageUpdateEvent(
    open val event: MessageUpdateEvent,
    open val channelId: Snowflake,
    open val messageId: Snowflake,
    open val old: Message?,
    open val new: DiscordPartialMessage,
    open val author: Member?,
    open val repliedToMessage: Message?,
    override val shard: Int,
    override val supplier: EntitySupplier = event.kord.defaultSupplier,
) : KordExEvent, Strategizable {
    /** @suppress Forwards to [repliedToMessage], **/
    val referencedMessage get() = repliedToMessage

    /** Channel behaviour representing the channel the message was sent to. **/
    val channel: MessageChannelBehavior
        get() = MessageChannelBehavior(id = channelId, kord = kord)

    /** Message behaviour representing the message that was updated.. **/
    val message: MessageBehavior
        get() = MessageBehavior(messageId = messageId, channelId = channelId, kord = kord)

    /** Attempt to retrieve the updated message object, throwing if not found. **/
    suspend fun getMessage(): Message =
        supplier.getMessage(channelId = channelId, messageId = messageId)

    /** Attempt to retrieve the updated message object, returning null if not found. **/
    suspend fun getMessageOrNull(): Message? =
        supplier.getMessageOrNull(channelId = channelId, messageId = messageId)
}

/**
 * A [MessageUpdateEvent] wrapper that represents a message that was proxied by PluralKit.
 *
 * @property pkMessage The PluralKit message object with metadata about the proxied message.
 */
class ProxiedMessageUpdateEvent(
    event: MessageUpdateEvent,
    channelId: Snowflake,
    messageId: Snowflake,
    old: Message?,
    new: DiscordPartialMessage,
    override val author: Member,
    repliedToMessage: Message?,
    shard: Int,
    val pkMessage: PKMessage,
    supplier: EntitySupplier = event.kord.defaultSupplier,
) : PKMessageUpdateEvent(event, channelId, messageId, old, new, author, repliedToMessage, shard, supplier) {
    override fun withStrategy(strategy: EntitySupplyStrategy<*>): ProxiedMessageUpdateEvent {
        val strategizedEvent = event.withStrategy(strategy)

        return ProxiedMessageUpdateEvent(
            strategizedEvent,
            channelId,
            messageId,
            strategizedEvent.old,
            strategizedEvent.new,
            author,
            repliedToMessage,
            shard,
            pkMessage,
            strategy.supply(kord)
        )
    }

    override fun toString(): String =
        "ProxiedMessageUpdateEvent(" +
            "event=$event, " +
            "channelId=$channelId, " +
            "messageId=$messageId, " +
            "old=$old, " +
            "new=$new, " +
            "member=$author, " +
            "shard=$shard, " +
            "supplier=$supplier" +
            ")"
}

/**
 * A [MessageUpdateEvent] wrapper that represents a message that was **not** proxied by PluralKit.
 */
class UnProxiedMessageUpdateEvent(
    event: MessageUpdateEvent,
    channelId: Snowflake,
    messageId: Snowflake,
    old: Message?,
    new: DiscordPartialMessage,
    author: Member?,
    repliedToMessage: Message?,
    shard: Int,
    supplier: EntitySupplier = event.kord.defaultSupplier,
) : PKMessageUpdateEvent(event, channelId, messageId, old, new, author, repliedToMessage, shard, supplier) {
    override fun withStrategy(strategy: EntitySupplyStrategy<*>): UnProxiedMessageUpdateEvent {
        val strategizedEvent = event.withStrategy(strategy)

        return UnProxiedMessageUpdateEvent(
            strategizedEvent,
            channelId,
            messageId,
            strategizedEvent.old,
            strategizedEvent.new,
            author,
            repliedToMessage,
            shard,
            strategy.supply(kord)
        )
    }

    override fun toString(): String =
        "UnProxiedMessageUpdateEvent(" +
            "event=$event, " +
            "channelId=$channelId, " +
            "messageId=$messageId, " +
            "old=$old, " +
            "new=$new, " +
            "member=$author, " +
            "shard=$shard, " +
            "supplier=$supplier" +
            ")"
}

internal suspend fun MessageUpdateEvent.proxied(p: PKMessage, referencedMessage: Message?): ProxiedMessageUpdateEvent {
    val member = kord.getChannelOf<GuildChannel>(channelId)!!.getGuild().getMemberOrNull(p.sender)!!

    return ProxiedMessageUpdateEvent(
        this,
        channelId,
        messageId,
        old,
        new,
        member,
        referencedMessage,
        shard,
        p,
    )
}

internal suspend fun MessageUpdateEvent.unproxied(): UnProxiedMessageUpdateEvent =
    UnProxiedMessageUpdateEvent(
        this,
        channelId,
        messageId,
        old,
        new,
        message.asMessageOrNull()?.getAuthorAsMember(),
        message.asMessageOrNull()?.referencedMessage,
        shard,
    )
