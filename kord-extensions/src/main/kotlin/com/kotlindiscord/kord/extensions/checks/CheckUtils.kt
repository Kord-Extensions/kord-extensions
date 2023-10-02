/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

@file:OptIn(KordUnsafe::class, KordExperimental::class)

package com.kotlindiscord.kord.extensions.checks

import com.kotlindiscord.kord.extensions.checks.types.CheckContext
import com.kotlindiscord.kord.extensions.events.interfaces.*
import com.kotlindiscord.kord.extensions.utils.authorId
import dev.kord.common.annotation.KordExperimental
import dev.kord.common.annotation.KordUnsafe
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.*
import dev.kord.core.behavior.channel.ChannelBehavior
import dev.kord.core.behavior.channel.threads.ThreadChannelBehavior
import dev.kord.core.cache.data.toData
import dev.kord.core.entity.Member
import dev.kord.core.entity.interaction.Interaction
import dev.kord.core.event.Event
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
        is ChannelEvent -> event.channel

        is ChannelCreateEvent -> event.channel
        is ChannelDeleteEvent -> event.channel
        is ChannelPinsUpdateEvent -> event.channel
        is ChannelUpdateEvent -> event.channel
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
//        is ThreadListSyncEvent -> event.
        is ThreadMemberUpdateEvent -> event.member.getThreadOrNull()
//        is ThreadMembersUpdateEvent -> event.

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
 * Retrieves a channel ID representing a channel that is the subject of a given event, if possible.
 *
 * This function only supports a specific set of events - any unsupported events will
 * simply result in a `null` value. Please note that some events may support a
 * null value for this type of object, and this will also be reflected in the return
 * value.
 *
 * @param event The event concerning to the channel to retrieve.
 * @return A [Long] representing the channel ID, or null if there isn't one.
 */
public suspend fun channelIdFor(event: Event): ULong? {
    return when (event) {
        is ChannelEvent -> event.channel?.id?.value

        is ChannelCreateEvent -> event.channel.id.value
        is ChannelDeleteEvent -> event.channel.id.value
        is ChannelPinsUpdateEvent -> event.channel.id.value
        is ChannelUpdateEvent -> event.channel.id.value
        is InteractionCreateEvent -> event.interaction.channel.id.value
        is InviteCreateEvent -> event.channel.id.value
        is InviteDeleteEvent -> event.channel.id.value
        is MessageBulkDeleteEvent -> event.channelId.value
        is MessageCreateEvent -> event.message.channel.id.value
        is MessageDeleteEvent -> event.channelId.value
        is MessageUpdateEvent -> event.channel.id.value
        is ReactionAddEvent -> event.channel.id.value
        is ReactionRemoveAllEvent -> event.channel.id.value
        is ReactionRemoveEmojiEvent -> event.channel.id.value
        is ReactionRemoveEvent -> event.channel.id.value
        is TypingStartEvent -> event.channel.id.value
        is VoiceStateUpdateEvent -> event.state.channelId?.value
        is WebhookUpdateEvent -> event.channel.id.value

        is ThreadChannelDeleteEvent -> event.channel.id.value
//        is ThreadListSyncEvent -> event.
        is ThreadMemberUpdateEvent -> event.member.getThreadOrNull()?.id?.value
//        is ThreadMembersUpdateEvent -> event.

        else -> null
    }
}

/**
 * Retrieves a channel ID representing a channel that is the subject of a given event, if possible.
 *
 * This function only supports a specific set of events - any unsupported events will
 * simply result in a `null` value. Please note that some events may support a
 * null value for this type of object, and this will also be reflected in the return
 * value.
 *
 * @param event The event concerning to the channel to retrieve.
 * @return A [Snowflake] representing the channel ID, or null if there isn't one.
 */
public suspend fun channelSnowflakeFor(event: Event): Snowflake? {
    return when (event) {
        is ChannelEvent -> event.channel?.id

        is ChannelCreateEvent -> event.channel.id
        is ChannelDeleteEvent -> event.channel.id
        is ChannelPinsUpdateEvent -> event.channel.id
        is ChannelUpdateEvent -> event.channel.id
        is InteractionCreateEvent -> event.interaction.channel.id
        is InviteCreateEvent -> event.channel.id
        is InviteDeleteEvent -> event.channel.id
        is MessageBulkDeleteEvent -> event.channelId
        is MessageCreateEvent -> event.message.channel.id
        is MessageDeleteEvent -> event.channelId
        is MessageUpdateEvent -> event.channel.id
        is ReactionAddEvent -> event.channel.id
        is ReactionRemoveAllEvent -> event.channel.id
        is ReactionRemoveEmojiEvent -> event.channel.id
        is ReactionRemoveEvent -> event.channel.id
        is TypingStartEvent -> event.channel.id
        is VoiceStateUpdateEvent -> event.state.channelId
        is WebhookUpdateEvent -> event.channel.id

        is ThreadChannelDeleteEvent -> event.channel.id
//        is ThreadListSyncEvent -> event.
        is ThreadMemberUpdateEvent -> event.member.getThreadOrNull()?.id
//        is ThreadMembersUpdateEvent -> event.

        else -> null
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
        is GuildEvent -> event.guild

        is BanAddEvent -> event.guild
        is BanRemoveEvent -> event.guild

        is CategoryCreateEvent -> event.channel.guild
        is CategoryDeleteEvent -> event.channel.guild
        is CategoryUpdateEvent -> event.channel.guild
        is EmojisUpdateEvent -> event.guild
        is GuildCreateEvent -> event.guild
        is GuildDeleteEvent -> event.guild
        is GuildUpdateEvent -> event.guild
        is IntegrationsUpdateEvent -> event.guild

        is InteractionCreateEvent -> {
            val guildId = event.interaction.data.guildId.value
                ?: return null

            event.kord.unsafe.guild(guildId)
        }

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
        is ReactionAddEvent -> event.guild
        is ReactionRemoveEvent -> event.guild
        is TextChannelCreateEvent -> event.channel.guild
        is TextChannelDeleteEvent -> event.channel.guild
        is TextChannelUpdateEvent -> event.channel.guild
        is TypingStartEvent -> event.guild
        is VoiceChannelCreateEvent -> event.channel.guild
        is VoiceChannelDeleteEvent -> event.channel.guild
        is VoiceChannelUpdateEvent -> event.channel.guild
        is VoiceServerUpdateEvent -> event.guild
        is VoiceStateUpdateEvent -> event.state.getGuildOrNull()
        is WebhookUpdateEvent -> event.guild

        is ThreadChannelCreateEvent -> event.channel.guild
        is ThreadUpdateEvent -> event.channel.guild
//        is ThreadChannelDeleteEvent -> event.
        is ThreadListSyncEvent -> event.guild
        is ThreadMemberUpdateEvent -> event.member.getThreadOrNull()?.guild
//        is ThreadMembersUpdateEvent -> event.

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
        is MemberEvent -> event.member

        is InteractionCreateEvent -> {
            val guildId = event.interaction.data.guildId.value
                ?: return null

            event.kord.unsafe
                .guild(guildId)
                .getMemberOrNull(event.interaction.user.id)
        }

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
//        event is ThreadUpdateEvent -> event.
//        event is ThreadChannelDeleteEvent -> event.
//        event is ThreadListSyncEvent -> event.
        is ThreadMemberUpdateEvent -> {
            val thread = event.member.getThreadOrNull()
                ?: return null

            event.member.asMember(thread.guildId)
        }

        is VoiceStateUpdateEvent -> event.kord.unsafe.member(
            event.state.guildId,
            event.state.userId
        )

//        event is ThreadMembersUpdateEvent -> event.
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
        is MessageEvent -> event.message

        is MessageCreateEvent -> event.message
        is MessageDeleteEvent -> event.message
        is MessageUpdateEvent -> event.getMessage()
        is ReactionAddEvent -> event.message
        is ReactionRemoveAllEvent -> event.message
        is ReactionRemoveEmojiEvent -> event.message
        is ReactionRemoveEvent -> event.message

        is ThreadChannelCreateEvent -> event.channel.getLastMessage()
//        is ThreadUpdateEvent -> event.
//        is ThreadChannelDeleteEvent -> event.
//        is ThreadListSyncEvent -> event.
//        is ThreadMemberUpdateEvent -> event.
//        is ThreadMembersUpdateEvent -> event.

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
 * @return A [RoleBehavior] representing the role, or null if there isn't one.
 */
public fun roleFor(event: Event): RoleBehavior? {
    return when (event) {
        is RoleEvent -> event.role

        is RoleCreateEvent -> event.role
        is RoleDeleteEvent -> event.role
        is RoleUpdateEvent -> event.role

//        is ThreadChannelCreateEvent -> event.
//        is ThreadUpdateEvent -> event.
//        is ThreadChannelDeleteEvent -> event.
//        is ThreadListSyncEvent -> event.
//        is ThreadMemberUpdateEvent -> event.
//        is ThreadMembersUpdateEvent -> event.

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
        is UserEvent -> event.user

        is BanAddEvent -> event.user
        is BanRemoveEvent -> event.user

        // We don't deal with self-bots, so we only want the first user - bots can't be in group DMs.
        is DMChannelCreateEvent -> event.channel.recipients.first { it.id != event.kord.selfId }
        is DMChannelDeleteEvent -> event.channel.recipients.first { it.id != event.kord.selfId }
        is DMChannelUpdateEvent -> event.channel.recipients.first { it.id != event.kord.selfId }

        is InteractionCreateEvent -> event.interaction.user
        is MemberJoinEvent -> event.member
        is MemberLeaveEvent -> event.user
        is MemberUpdateEvent -> event.member
        is MessageCreateEvent -> event.message.author
        is MessageDeleteEvent -> event.message?.author
        is MessageUpdateEvent -> event.getMessage().author
        is PresenceUpdateEvent -> event.member
        is ReactionAddEvent -> event.user
        is ReactionRemoveEvent -> event.user
        is TypingStartEvent -> event.user
        is UserUpdateEvent -> event.user

        is VoiceStateUpdateEvent -> event.kord.unsafe.user(event.state.userId)

        is ThreadChannelCreateEvent -> event.channel.owner
//        is ThreadUpdateEvent -> event.
//        is ThreadChannelDeleteEvent -> event.
//        is ThreadListSyncEvent -> event.
        is ThreadMemberUpdateEvent -> event.member
//        is ThreadMembersUpdateEvent -> event.

        else -> null
    }
}

/** Silence the current check by removing any message it may have set. **/
public fun CheckContext<*>.silence() {
    message = null
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
