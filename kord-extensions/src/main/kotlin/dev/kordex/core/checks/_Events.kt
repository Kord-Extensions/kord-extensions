/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

@file:OptIn(KordUnsafe::class, KordExperimental::class)

package dev.kordex.core.checks

import dev.kord.common.annotation.KordExperimental
import dev.kord.common.annotation.KordUnsafe
import dev.kord.core.behavior.*
import dev.kord.core.behavior.channel.ChannelBehavior
import dev.kord.core.behavior.channel.threads.ThreadChannelBehavior
import dev.kord.core.cache.data.toData
import dev.kord.core.entity.Member
import dev.kord.core.entity.interaction.GuildInteraction
import dev.kord.core.entity.interaction.Interaction
import dev.kord.core.event.Event
import dev.kord.core.event.automoderation.AutoModerationActionExecutionEvent
import dev.kord.core.event.automoderation.AutoModerationEvent
import dev.kord.core.event.channel.*
import dev.kord.core.event.channel.thread.*
import dev.kord.core.event.guild.*
import dev.kord.core.event.interaction.InteractionCreateEvent
import dev.kord.core.event.message.*
import dev.kord.core.event.role.RoleCreateEvent
import dev.kord.core.event.role.RoleDeleteEvent
import dev.kord.core.event.role.RoleUpdateEvent
import dev.kord.core.event.user.PresenceUpdateEvent
import dev.kord.core.event.user.UserUpdateEvent
import dev.kord.core.event.user.VoiceStateUpdateEvent
import dev.kordex.core.events.interfaces.*
import dev.kordex.core.utils.authorId
import kotlinx.coroutines.flow.first

/**
 * Retrieves a channel that is the subject of a given event, if possible.
 *
 * This function only supports a specific set of events - any unsupported events will
 * simply result in a `null` value. Please note that some events may support a
 * null value for this type of object, and this will also be reflected in the return
 * value.
 *
 * @param event The event concerning to the channel to retrieve.
 * @return A [ChannelBehavior] representing the channel, or null if there isn't one.
 */
public suspend fun channelFor(event: Event): ChannelBehavior? {
	return when (event) {
		// KordEx generic event interface
		is ChannelEvent -> event.channel

		is AutoModerationActionExecutionEvent -> event.channel
		is ChannelCreateEvent -> event.channel
		is ChannelDeleteEvent -> event.channel
		is ChannelPinsUpdateEvent -> event.channel
		is ChannelUpdateEvent -> event.channel

		is GuildAuditLogEntryCreateEvent -> if (event.auditLogEntry.options?.channelId?.value != null) {
			event.kord.unsafe.channel(event.auditLogEntry.options!!.channelId.value!!)
		} else {
			null
		}

		is GuildScheduledEventEvent -> if (event.channelId != null) {
			event.kord.unsafe.channel(event.channelId!!)
		} else {
			null
		}

		is InteractionCreateEvent -> event.interaction.channel
		is InviteCreateEvent -> event.channel
		is InviteDeleteEvent -> event.channel
		is MessageBulkDeleteEvent -> event.channel
		is MessageCreateEvent -> event.message.channel
		is MessageDeleteEvent -> event.message?.channel
		is MessageUpdateEvent -> event.channel
		is ReactionAddEvent -> event.channel
		is ReactionRemoveAllEvent -> event.channel
		is ReactionRemoveEmojiEvent -> event.channel
		is ReactionRemoveEvent -> event.channel
		is TypingStartEvent -> event.channel
		is VoiceStateUpdateEvent -> event.kord.unsafe.channel(event.state.channelId ?: return null)
		is WebhookUpdateEvent -> event.channel
		is ThreadChannelDeleteEvent -> event.old
		is ThreadMemberUpdateEvent -> event.member.getThreadOrNull()
		is ThreadMembersUpdateEvent -> event.kord.unsafe.channel(event.id)
		is ThreadUpdateEvent -> event.channel

		else -> null
	}
}

/**
 * Retrieves a channel that is the subject of a given event, if possible, returning the
 * parent if the channel is a thread.
 *
 * This function only supports a specific set of events - any unsupported events will
 * simply result in a `null` value. Please note that some events may support a
 * null value for this type of object, and this will also be reflected in the return
 * value.
 *
 * @param event The event concerning to the channel to retrieve.
 * @return A [ChannelBehavior] representing the channel, or null if there isn't one.
 */
public suspend fun topChannelFor(event: Event): ChannelBehavior? {
	val channel = channelFor(event)?.asChannelOrNull() ?: return null

	return if (channel is ThreadChannelBehavior) {
		channel.parent
	} else {
		channel
	}
}

/**
 * Retrieves a guild that is the subject of a given event, if possible.
 *
 * This function only supports a specific set of events - any unsupported events will
 * simply result in a `null` value. Please note that some events may support a
 * null value for this type of object, and this will also be reflected in the return
 * value.
 *
 * @param event The event concerning to the channel to retrieve.
 * @return A [GuildBehavior] representing the guild, or null if there isn't one.
 */
public suspend fun guildFor(event: Event): GuildBehavior? {
	return when (event) {
		// KordEx generic event interface
		is GuildEvent -> event.guild

		is AutoModerationEvent -> event.guild
		is BanAddEvent -> event.guild
		is BanRemoveEvent -> event.guild
		is CategoryCreateEvent -> event.channel.guild
		is CategoryDeleteEvent -> event.channel.guild
		is CategoryUpdateEvent -> event.channel.guild
		is EmojisUpdateEvent -> event.guild
		is GuildCreateEvent -> event.guild
		is GuildDeleteEvent -> event.guild
		is GuildScheduledEventEvent -> event.kord.unsafe.guild(event.guildId)
		is GuildUpdateEvent -> event.guild
		is IntegrationCreateEvent -> event.guild
		is IntegrationDeleteEvent -> event.guild
		is IntegrationUpdateEvent -> event.guild
		is IntegrationsUpdateEvent -> event.guild
		is InteractionCreateEvent -> (event.interaction as? GuildInteraction)?.guild
		is InviteCreateEvent -> event.guild
		is InviteDeleteEvent -> event.guild
		is MembersChunkEvent -> event.guild
		is MemberJoinEvent -> event.guild
		is MemberLeaveEvent -> event.guild
		is MemberUpdateEvent -> event.guild
		is MessageBulkDeleteEvent -> event.guild

		is MessageCreateEvent -> {
			val guildId = event.message.data.guildId.value
				?: return null

			event.kord.unsafe.guild(guildId)
		}

		is MessageDeleteEvent -> event.guild

		is MessageUpdateEvent -> {
			val guildId = event.new.guildId.value
				?: return null

			event.kord.unsafe.guild(guildId)
		}

		is NewsChannelCreateEvent -> event.channel.guild
		is NewsChannelDeleteEvent -> event.channel.guild
		is NewsChannelUpdateEvent -> event.channel.guild
		is PresenceUpdateEvent -> event.guild
		is ReactionAddEvent -> event.guild
		is ReactionRemoveEvent -> event.guild
		is TextChannelCreateEvent -> event.channel.guild
		is TextChannelDeleteEvent -> event.channel.guild
		is TextChannelUpdateEvent -> event.channel.guild
		is ThreadChannelCreateEvent -> event.channel.guild
		is ThreadUpdateEvent -> event.channel.guild
		is ThreadChannelDeleteEvent -> event.channel.guild
		is ThreadListSyncEvent -> event.guild
		is ThreadMemberUpdateEvent -> event.member.getThreadOrNull()?.guild
		is ThreadMembersUpdateEvent -> event.kord.unsafe.guild(event.guildId)
		is TypingStartEvent -> event.guild
		is VoiceChannelCreateEvent -> event.channel.guild
		is VoiceChannelDeleteEvent -> event.channel.guild
		is VoiceChannelUpdateEvent -> event.channel.guild
		is VoiceServerUpdateEvent -> event.guild
		is VoiceStateUpdateEvent -> event.state.getGuildOrNull()
		is WebhookUpdateEvent -> event.guild

		// TODO: Kord doesn't have the guild yet!
// 		is GuildAuditLogEntryCreateEvent -> event.auditLogEntry.userId

		else -> null
	}
}

/**
 * Retrieves a member that is the subject of a given event, if possible.
 *
 * This function only supports a specific set of events - any unsupported events will
 * simply result in a `null` value. Please note that some events may support a
 * null value for this type of object, and this will also be reflected in the return
 * value.
 *
 * @param event The event concerning to the channel to retrieve.
 * @return A [MemberBehavior] representing the member, or null if there isn't one.
 */
public suspend fun memberFor(event: Event): MemberBehavior? {
	return when (event) {
		// KordEx generic event interface
		is MemberEvent -> event.member

		is AutoModerationActionExecutionEvent -> event.member

		is GuildScheduledEventEvent -> if (event.scheduledEvent.creatorId != null) {
			event.kord.unsafe.member(event.guildId, event.scheduledEvent.creatorId!!)
		} else {
			null
		}

		is InteractionCreateEvent -> (event.interaction as? GuildInteraction)?.user
		is InviteCreateEvent -> event.inviterMember

		is MemberJoinEvent -> event.member
		is MemberUpdateEvent -> event.member
		is MessageCreateEvent -> event.member

		is MessageDeleteEvent ->
			event.message?.data?.guildId?.value
				?.let { event.kord.unsafe.member(it, event.message!!.data.authorId) }

		is MessageUpdateEvent -> {
			val message = event.new

			if (message.author.value != null && message.member.value != null) {
				val userData = message.author.value!!.toData()
				val memberData = message.member.value!!.toData(userData.id, event.new.guildId.value!!)

				return Member(memberData, userData, event.kord)
			}

			null
		}

		is ReactionAddEvent -> event.userAsMember
		is ReactionRemoveEvent -> event.userAsMember
		is TypingStartEvent -> event.getGuildOrNull()?.getMemberOrNull(event.userId)
		is ThreadChannelCreateEvent -> event.channel.owner.asMember(event.channel.guildId)

		is ThreadChannelDeleteEvent -> if (event.channel.data.ownerId.value != null) {
			event.kord.unsafe.member(event.channel.guildId, event.channel.data.ownerId.value!!)
		} else {
			null
		}

		is ThreadMemberUpdateEvent -> {
			val thread = event.member.getThreadOrNull()
				?: return null

			event.member.asMember(thread.guildId)
		}

		is VoiceStateUpdateEvent -> event.kord.unsafe.member(
			event.state.guildId,
			event.state.userId
		)

		else -> null
	}
}

/**
 * Retrieves a message that is the subject of a given event, if possible.
 *
 * This function only supports a specific set of events - any unsupported events will
 * simply result in a `null` value. Please note that some events may support a
 * null value for this type of object, and this will also be reflected in the return
 * value.
 *
 * @param event The event concerning to the channel to retrieve.
 * @return A [MessageBehavior] representing the message, or null if there isn't one.
 */
public suspend fun messageFor(event: Event): MessageBehavior? {
	return when (event) {
		// KordEx generic event interface
		is MessageEvent -> event.message

		is AutoModerationActionExecutionEvent -> event.message

		is GuildAuditLogEntryCreateEvent -> if (
			event.auditLogEntry.options?.channelId?.value != null &&
			event.auditLogEntry.options?.messageId?.value != null
		) {
			event.kord.unsafe.message(
				event.auditLogEntry.options!!.channelId.value!!,
				event.auditLogEntry.options!!.messageId.value!!
			)
		} else {
			null
		}

		is MessageCreateEvent -> event.message
		is MessageDeleteEvent -> event.message
		is MessageUpdateEvent -> event.getMessage()
		is ReactionAddEvent -> event.message
		is ReactionRemoveAllEvent -> event.message
		is ReactionRemoveEmojiEvent -> event.message
		is ReactionRemoveEvent -> event.message

		is ThreadChannelCreateEvent -> event.channel.getLastMessage()

		else -> null
	}
}

/**
 * Retrieves a role that is the subject of a given event, if possible.
 *
 * This function only supports a specific set of events - any unsupported events will
 * simply result in a `null` value. Please note that some events may support a
 * null value for this type of object, and this will also be reflected in the return
 * value.
 *
 * @param event The event concerning to the channel to retrieve.
 * @return A [RoleBehavior] representing the role, or null if missing.
 */
public fun roleFor(event: Event): RoleBehavior? {
	return when (event) {
		// KordEx generic event interface
		is RoleEvent -> event.role

		is RoleCreateEvent -> event.role
		is RoleDeleteEvent -> event.role
		is RoleUpdateEvent -> event.role

		else -> null
	}
}

/**
 * Retrieves a thread that is the subject of a given event, if possible.
 *
 * This function only supports a specific set of events - any unsupported events will
 * simply result in a `null` value. Please note that some events may support a
 * null value for this type of object, and this will also be reflected in the return
 * value.
 *
 * @param event The event concerning to the channel to retrieve.
 * @return A [ThreadChannelBehavior] representing the role, or null if there isn't one.
 */
public suspend fun threadFor(event: Event): ThreadChannelBehavior? =
	channelFor(event) as? ThreadChannelBehavior

/**
 * Retrieves a user that is the subject of a given event, if possible.
 *
 * This function only supports a specific set of events - any unsupported events will
 * simply result in a `null` value. Please note that some events may support a
 * null value for this type of object, and this will also be reflected in the return
 * value.
 *
 * @param event The event concerning to the channel to retrieve.
 * @return A [UserBehavior] representing the user, or null if there isn't one.
 */
public suspend fun userFor(event: Event): UserBehavior? {
	return when (event) {
		// KordEx generic event interface
		is UserEvent -> event.user

		is AutoModerationActionExecutionEvent -> event.member

		is BanAddEvent -> event.user
		is BanRemoveEvent -> event.user

		// We don't deal with self-bots, so we only want the first user - bots can't be in group DMs.
		is DMChannelCreateEvent -> event.channel.recipients.first { it.id != event.kord.selfId }
		is DMChannelDeleteEvent -> event.channel.recipients.first { it.id != event.kord.selfId }
		is DMChannelUpdateEvent -> event.channel.recipients.first { it.id != event.kord.selfId }

		is GuildAuditLogEntryCreateEvent -> if (
			event.auditLogEntry.userId != null
		) {
			event.kord.unsafe.user(event.auditLogEntry.userId!!)
		} else {
			null
		}

		is GuildScheduledEventEvent -> if (event.scheduledEvent.creatorId != null) {
			event.kord.unsafe.user(event.scheduledEvent.creatorId!!)
		} else {
			null
		}

		is InteractionCreateEvent -> event.interaction.user
		is InviteCreateEvent -> event.inviter
		is MemberJoinEvent -> event.member
		is MemberLeaveEvent -> event.user
		is MemberUpdateEvent -> event.member
		is MessageCreateEvent -> event.message.author
		is MessageDeleteEvent -> event.message?.author
		is MessageUpdateEvent -> event.getMessage().author
		is PresenceUpdateEvent -> event.member
		is ReactionAddEvent -> event.user
		is ReactionRemoveEvent -> event.user
		is ThreadChannelCreateEvent -> event.channel.owner

		is ThreadChannelDeleteEvent -> if (event.channel.data.ownerId.value != null) {
			event.kord.unsafe.user(event.channel.data.ownerId.value!!)
		} else {
			null
		}

		is ThreadMemberUpdateEvent -> event.member
		is TypingStartEvent -> event.user
		is UserUpdateEvent -> event.user
		is VoiceStateUpdateEvent -> event.kord.unsafe.user(event.state.userId)

		else -> null
	}
}

/**
 * Retrieves an interaction that is the subject of a given event, if possible.
 *
 * This function only supports a specific set of events - any unsupported events will
 * simply result in a `null` value. Please note that some events may support a
 * null value for this type of object, and this will also be reflected in the return
 * value.
 *
 * @param event The event concerning to the interaction to retrieve.
 * @return A [Interaction] representing the interaction, or null if there isn't one.
 */
public fun interactionFor(event: Event): Interaction? = (event as? InteractionCreateEvent)?.interaction
