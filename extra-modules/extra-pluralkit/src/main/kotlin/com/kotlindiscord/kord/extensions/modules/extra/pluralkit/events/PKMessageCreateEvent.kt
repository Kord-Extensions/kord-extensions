/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.modules.extra.pluralkit.events

import com.kotlindiscord.kord.extensions.events.KordExEvent
import com.kotlindiscord.kord.extensions.modules.extra.pluralkit.api.PKMessage
import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.Guild
import dev.kord.core.entity.Member
import dev.kord.core.entity.Message
import dev.kord.core.entity.Strategizable
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.supplier.EntitySupplier
import dev.kord.core.supplier.EntitySupplyStrategy

/**
 * A [MessageCreateEvent] wrapper that fires with extra context that may be supplied by PluralKit. Subtypes are
 * available if you need to be more specific about whether the message was proxied or not.
 *
 * @property event The original event that triggered this one.
 * @property message The message object in question, which may be a webhook message if proxied.
 * @property guildId The ID for the guild that this message was sent on, if any.
 * @property author The original account that sent the message, even if it was proxied.
 * @property repliedToMessage The original message that was replied to, even if this one was proxied.
 */
sealed class PKMessageCreateEvent(
    open val event: MessageCreateEvent,
    open val message: Message,
    open val guildId: Snowflake?,
    open val author: Member?,
    open val repliedToMessage: Message?,
    override val shard: Int,
    override val supplier: EntitySupplier = event.kord.defaultSupplier,
) : KordExEvent, Strategizable {
    /** @suppress Forwards to [repliedToMessage], **/
    val referencedMessage get() = repliedToMessage

    /** Attempt to retrieve the guild this message was sent in, if any. **/
    suspend fun getGuild(): Guild? = guildId?.let { supplier.getGuildOrNull(it) }
}

/**
 * A [MessageCreateEvent] wrapper that represents a message that was proxied by PluralKit.
 *
 * @property pkMessage The PluralKit message object with metadata about the proxied message.
 */
class ProxiedMessageCreateEvent(
    event: MessageCreateEvent,
    message: Message,
    guildId: Snowflake?,
    override val author: Member,
    repliedToMessage: Message?,
    shard: Int,
    val pkMessage: PKMessage,
    supplier: EntitySupplier = event.kord.defaultSupplier,
) : PKMessageCreateEvent(event, message, guildId, author, repliedToMessage, shard, supplier) {
    override fun withStrategy(strategy: EntitySupplyStrategy<*>): PKMessageCreateEvent {
        val strategizedEvent = event.withStrategy(strategy)

        return ProxiedMessageCreateEvent(
            strategizedEvent,
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
        "ProxiedMessageCreateEvent(" +
            "event=$event, " +
            "message=$message, " +
            "guildId=$guildId, " +
            "author=$author, " +
            "repliedToMessage=$repliedToMessage, " +
            "shard=$shard, " +
            "supplier=$supplier" +
            ")"
}

/**
 * A [MessageCreateEvent] wrapper that represents a message that was **not** proxied by PluralKit.
 */
class UnProxiedMessageCreateEvent(
    event: MessageCreateEvent,
    message: Message,
    guildId: Snowflake?,
    author: Member?,
    repliedToMessage: Message?,
    shard: Int,
    supplier: EntitySupplier = event.kord.defaultSupplier,
) : PKMessageCreateEvent(event, message, guildId, author, repliedToMessage, shard, supplier) {
    override fun withStrategy(strategy: EntitySupplyStrategy<*>): PKMessageCreateEvent {
        val strategizedEvent = event.withStrategy(strategy)

        return UnProxiedMessageCreateEvent(
            strategizedEvent,
            strategizedEvent.message,
            guildId,
            author,
            repliedToMessage,
            shard,
            strategy.supply(kord),
        )
    }

    override fun toString(): String =
        "UnProxiedMessageCreateEvent(" +
            "event=$event, " +
            "message=$message, " +
            "guildId=$guildId, " +
            "author=$author, " +
            "repliedToMessage=$repliedToMessage, " +
            "shard=$shard, " +
            "supplier=$supplier" +
            ")"
}

internal suspend fun MessageCreateEvent.proxied(p: PKMessage, referencedMessage: Message?): ProxiedMessageCreateEvent {
    val member = getGuild()!!.getMemberOrNull(p.sender)!!

    return ProxiedMessageCreateEvent(
        this,
        message,
        guildId,
        member,
        referencedMessage,
        shard,
        p,
    )
}

internal suspend fun MessageCreateEvent.unproxied(): UnProxiedMessageCreateEvent =
    UnProxiedMessageCreateEvent(
        this,
        message,
        guildId,
        member,
        message.referencedMessage,
        shard
    )
