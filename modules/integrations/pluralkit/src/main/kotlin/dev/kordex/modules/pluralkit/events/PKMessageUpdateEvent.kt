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
import dev.kord.common.entity.DiscordPartialMessage
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.GuildBehavior
import dev.kord.core.behavior.MemberBehavior
import dev.kord.core.behavior.MessageBehavior
import dev.kord.core.behavior.UserBehavior
import dev.kord.core.behavior.channel.MessageChannelBehavior
import dev.kord.core.entity.*
import dev.kord.core.entity.channel.Channel
import dev.kord.core.entity.channel.GuildChannel
import dev.kord.core.event.message.MessageUpdateEvent
import dev.kord.core.supplier.EntitySupplier
import dev.kord.core.supplier.EntitySupplyStrategy
import dev.kordex.core.events.KordExEvent
import dev.kordex.core.events.interfaces.ChannelEvent
import dev.kordex.core.events.interfaces.MemberEvent
import dev.kordex.core.events.interfaces.MessageEvent
import dev.kordex.modules.pluralkit.api.PKMessage

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
) : KordExEvent, Strategizable, ChannelEvent, MessageEvent, MemberEvent {
	/** @suppress Forwards to [repliedToMessage], **/
	val referencedMessage get() = repliedToMessage

	override val channel: MessageChannelBehavior get() = kord.unsafe.messageChannel(channelId)
	override val guild: GuildBehavior? get() = member?.guild
	override val member: MemberBehavior? get() = author
	override val message: MessageBehavior get() = kord.unsafe.message(messageId = messageId, channelId = channelId)
	override val user: UserBehavior? get() = author

	override suspend fun getChannel(): Channel = channel.asChannel()
	override suspend fun getChannelOrNull(): Channel = channel.asChannel()

	override suspend fun getGuild(): Guild = getGuildOrNull()!!
	override suspend fun getGuildOrNull(): Guild? = member?.guild?.asGuildOrNull()

	override suspend fun getMember(): Member = author!!
	override suspend fun getMemberOrNull(): Member? = author

	override suspend fun getMessage(): Message =
		supplier.getMessage(channelId = channelId, messageId = messageId)

	override suspend fun getMessageOrNull(): Message? =
		supplier.getMessageOrNull(channelId = channelId, messageId = messageId)

	override suspend fun getUser(): User = author!!
	override suspend fun getUserOrNull(): User? = author
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
		message.asMessageOrNull()?.getAuthorAsMemberOrNull(),
		message.asMessageOrNull()?.referencedMessage,
		shard,
	)
