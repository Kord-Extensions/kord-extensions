@file:OptIn(KordPreview::class)

package com.kotlindiscord.kord.extensions.checks

import dev.kord.common.annotation.KordPreview
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.*
import dev.kord.core.behavior.channel.ChannelBehavior
import dev.kord.core.entity.interaction.GuildInteraction
import dev.kord.core.event.Event
import dev.kord.core.event.channel.*
import dev.kord.core.event.guild.*
import dev.kord.core.event.interaction.InteractionCreateEvent
import dev.kord.core.event.message.*
import dev.kord.core.event.role.RoleCreateEvent
import dev.kord.core.event.role.RoleDeleteEvent
import dev.kord.core.event.role.RoleUpdateEvent
import dev.kord.core.event.user.PresenceUpdateEvent
import dev.kord.core.event.user.UserUpdateEvent
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
public fun channelFor(event: Event): ChannelBehavior? {
    return when (event) {
        is ChannelCreateEvent -> event.channel
        is ChannelDeleteEvent -> event.channel
        is ChannelPinsUpdateEvent -> event.channel
        is ChannelUpdateEvent -> event.channel
        is InteractionCreateEvent -> event.interaction.channel
        is InviteCreateEvent -> event.channel
        is InviteDeleteEvent -> event.channel
        is MessageCreateEvent -> event.message.channel
        is MessageDeleteEvent -> event.message?.channel
        is MessageUpdateEvent -> event.channel
        is ReactionAddEvent -> event.channel
        is ReactionRemoveAllEvent -> event.channel
        is ReactionRemoveEmojiEvent -> event.channel
        is ReactionRemoveEvent -> event.channel
        is TypingStartEvent -> event.channel
        is WebhookUpdateEvent -> event.channel

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
 * @return A [Long] representing the channel ID, or null if there isn't one.
 */
public fun channelIdFor(event: Event): Long? {
    return when (event) {
        is ChannelCreateEvent -> event.channel.id.value
        is ChannelDeleteEvent -> event.channel.id.value
        is ChannelPinsUpdateEvent -> event.channel.id.value
        is ChannelUpdateEvent -> event.channel.id.value
        is InteractionCreateEvent -> event.interaction.channel.id.value
        is InviteCreateEvent -> event.channel.id.value
        is InviteDeleteEvent -> event.channel.id.value
        is MessageCreateEvent -> event.message.channel.id.value
        is MessageDeleteEvent -> event.channelId.value
        is MessageUpdateEvent -> event.channel.id.value
        is ReactionAddEvent -> event.channel.id.value
        is ReactionRemoveAllEvent -> event.channel.id.value
        is ReactionRemoveEmojiEvent -> event.channel.id.value
        is ReactionRemoveEvent -> event.channel.id.value
        is TypingStartEvent -> event.channel.id.value
        is WebhookUpdateEvent -> event.channel.id.value

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
public fun channelSnowflakeFor(event: Event): Snowflake? {
    return when (event) {
        is ChannelCreateEvent -> event.channel.id
        is ChannelDeleteEvent -> event.channel.id
        is ChannelPinsUpdateEvent -> event.channel.id
        is ChannelUpdateEvent -> event.channel.id
        is InteractionCreateEvent -> event.interaction.channel.id
        is InviteCreateEvent -> event.channel.id
        is InviteDeleteEvent -> event.channel.id
        is MessageCreateEvent -> event.message.channel.id
        is MessageDeleteEvent -> event.channelId
        is MessageUpdateEvent -> event.channel.id
        is ReactionAddEvent -> event.channel.id
        is ReactionRemoveAllEvent -> event.channel.id
        is ReactionRemoveEmojiEvent -> event.channel.id
        is ReactionRemoveEvent -> event.channel.id
        is TypingStartEvent -> event.channel.id
        is WebhookUpdateEvent -> event.channel.id

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
        is InteractionCreateEvent -> (event.interaction as? GuildInteraction)?.guild
        is InviteCreateEvent -> event.guild
        is InviteDeleteEvent -> event.guild
        is MembersChunkEvent -> event.guild
        is MemberJoinEvent -> event.guild
        is MemberLeaveEvent -> event.guild
        is MemberUpdateEvent -> event.guild
        is MessageCreateEvent -> event.message.getGuildOrNull()
        is MessageDeleteEvent -> event.guild
        is MessageUpdateEvent -> event.getMessage().getGuildOrNull()
        is NewsChannelCreateEvent -> event.channel.guild
        is NewsChannelDeleteEvent -> event.channel.guild
        is NewsChannelUpdateEvent -> event.channel.guild
        is ReactionAddEvent -> event.guild
        is ReactionRemoveEvent -> event.guild
        is StoreChannelCreateEvent -> event.channel.guild
        is StoreChannelDeleteEvent -> event.channel.guild
        is StoreChannelUpdateEvent -> event.channel.guild
        is TextChannelCreateEvent -> event.channel.guild
        is TextChannelDeleteEvent -> event.channel.guild
        is TextChannelUpdateEvent -> event.channel.guild
        is TypingStartEvent -> event.guild
        is VoiceChannelCreateEvent -> event.channel.guild
        is VoiceChannelDeleteEvent -> event.channel.guild
        is VoiceChannelUpdateEvent -> event.channel.guild
        is VoiceServerUpdateEvent -> event.guild
        is WebhookUpdateEvent -> event.guild

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
    return when {
        event is InteractionCreateEvent -> (event.interaction as? GuildInteraction)?.member

        event is MemberJoinEvent -> event.member
        event is MemberUpdateEvent -> event.member

        event is MessageCreateEvent && event.message.getGuildOrNull() != null ->
            event.message.getAuthorAsMember()

        event is MessageDeleteEvent && event.message?.getGuildOrNull() != null ->
            event.message?.getAuthorAsMember()

        event is MessageUpdateEvent && event.message.asMessageOrNull()?.getGuildOrNull() != null ->
            event.getMessage().getAuthorAsMember()

        event is ReactionAddEvent && event.message.asMessageOrNull()?.getGuildOrNull() != null ->
            event.getUserAsMember()

        event is ReactionRemoveEvent && event.message.asMessageOrNull()?.getGuildOrNull() != null ->
            event.getUserAsMember()

        event is TypingStartEvent -> if (event.guildId != null) {
            event.getGuild()!!.getMemberOrNull(event.userId)
        } else {
            null
        }

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
        is MessageCreateEvent -> event.message
        is MessageDeleteEvent -> event.message
        is MessageUpdateEvent -> event.getMessage()
        is ReactionAddEvent -> event.message
        is ReactionRemoveAllEvent -> event.message
        is ReactionRemoveEmojiEvent -> event.message
        is ReactionRemoveEvent -> event.message

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
        is RoleCreateEvent -> event.role
        is RoleDeleteEvent -> event.role
        is RoleUpdateEvent -> event.role

        else -> null
    }
}

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
        is BanAddEvent -> event.user
        is BanRemoveEvent -> event.user

        // We don't deal with selfbots, so we only want the first user - bots can't be in group DMs.
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

        else -> null
    }
}
