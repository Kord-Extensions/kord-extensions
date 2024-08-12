/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

@file:OptIn(KordUnsafe::class, KordExperimental::class)

package dev.kordex.modules.pluralkit.events

import dev.kord.common.annotation.KordExperimental
import dev.kord.common.annotation.KordUnsafe
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.GuildBehavior
import dev.kord.core.behavior.MemberBehavior
import dev.kord.core.behavior.UserBehavior
import dev.kord.core.behavior.channel.ChannelBehavior
import dev.kord.core.entity.*
import dev.kord.core.entity.channel.Channel
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.supplier.EntitySupplier
import dev.kord.core.supplier.EntitySupplyStrategy
import dev.kordex.core.events.KordExEvent
import dev.kordex.core.events.interfaces.ChannelEvent
import dev.kordex.core.events.interfaces.GuildEvent
import dev.kordex.core.events.interfaces.MemberEvent
import dev.kordex.core.events.interfaces.MessageEvent
import dev.kordex.modules.pluralkit.api.PKMessage

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
	override val message: Message,
	open val guildId: Snowflake?,
	open val author: Member?,
	open val repliedToMessage: Message?,
	override val shard: Int,
	override val supplier: EntitySupplier = event.kord.defaultSupplier,
) : KordExEvent, Strategizable, MessageEvent, GuildEvent, MemberEvent, ChannelEvent {
	/** @suppress Forwards to [repliedToMessage], **/
	val referencedMessage get() = repliedToMessage

	override val guild: GuildBehavior? get() = guildId?.let { kord.unsafe.guild(it) }
	override val member: MemberBehavior? get() = author
	override val user: UserBehavior? get() = author
	override val channel: ChannelBehavior get() = message.channel

	override suspend fun getGuild(): Guild = getGuildOrNull()!!
	override suspend fun getGuildOrNull(): Guild? = guildId?.let { supplier.getGuildOrNull(it) }

	override suspend fun getMember(): Member = author!!
	override suspend fun getMemberOrNull(): Member? = author

	override suspend fun getMessage(): Message = message
	override suspend fun getMessageOrNull(): Message? = message

	override suspend fun getUser(): User = author!!
	override suspend fun getUserOrNull(): User? = author

	override suspend fun getChannel(): Channel = channel.asChannel()
	override suspend fun getChannelOrNull(): Channel? = getChannel()
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
	val member = getGuildOrNull()!!.getMemberOrNull(p.sender)!!

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

internal fun MessageCreateEvent.unproxied(): UnProxiedMessageCreateEvent =
	UnProxiedMessageCreateEvent(
		this,
		message,
		guildId,
		member,
		message.referencedMessage,
		shard
	)
