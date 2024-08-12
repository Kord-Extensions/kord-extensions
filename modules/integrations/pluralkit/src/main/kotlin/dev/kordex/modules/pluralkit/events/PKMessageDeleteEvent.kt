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
import dev.kord.core.behavior.channel.MessageChannelBehavior
import dev.kord.core.entity.*
import dev.kord.core.entity.channel.Channel
import dev.kord.core.event.message.MessageDeleteEvent
import dev.kord.core.supplier.EntitySupplier
import dev.kord.core.supplier.EntitySupplyStrategy
import dev.kordex.core.events.KordExEvent
import dev.kordex.core.events.interfaces.ChannelEvent
import dev.kordex.core.events.interfaces.GuildEvent
import dev.kordex.core.events.interfaces.MemberEvent
import dev.kordex.core.events.interfaces.MessageEvent
import dev.kordex.modules.pluralkit.api.PKMessage

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
	override val message: Message?,
	open val guildId: Snowflake?,
	open val author: Member?,
	open val repliedToMessage: Message?,
	override val shard: Int,
	override val supplier: EntitySupplier = event.kord.defaultSupplier,
) : KordExEvent, Strategizable, ChannelEvent, MessageEvent, GuildEvent, MemberEvent {
	/** @suppress Forwards to [repliedToMessage], **/
	val referencedMessage get() = repliedToMessage

	/** Channel behaviour representing the channel the message was sent to. **/
	override val channel: MessageChannelBehavior
		get() = MessageChannelBehavior(channelId, kord)

	override val guild: GuildBehavior? get() = guildId?.let { kord.unsafe.guild(it) }
	override val member: MemberBehavior? get() = author
	override val user: UserBehavior? get() = author

	override suspend fun getChannel(): Channel = channel.asChannel()
	override suspend fun getChannelOrNull(): Channel = channel.asChannel()

	override suspend fun getGuild(): Guild = getGuildOrNull()!!
	override suspend fun getGuildOrNull(): Guild? = guildId?.let { supplier.getGuildOrNull(it) }

	override suspend fun getMember(): Member = author!!
	override suspend fun getMemberOrNull(): Member? = author

	override suspend fun getMessage(): Message = message!!
	override suspend fun getMessageOrNull(): Message? = message

	override suspend fun getUser(): User = author!!
	override suspend fun getUserOrNull(): User? = author
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
	val member = getGuildOrNull()!!.getMemberOrNull(p.sender)!!

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
		message?.getAuthorAsMemberOrNull(),
		message?.referencedMessage,
		shard
	)
