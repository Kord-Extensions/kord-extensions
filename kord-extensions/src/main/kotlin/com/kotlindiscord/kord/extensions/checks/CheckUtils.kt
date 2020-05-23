package com.kotlindiscord.kord.extensions.checks

import com.gitlab.kordlib.core.behavior.*
import com.gitlab.kordlib.core.behavior.channel.ChannelBehavior
import com.gitlab.kordlib.core.event.Event
import com.gitlab.kordlib.core.event.UserUpdateEvent
import com.gitlab.kordlib.core.event.VoiceServerUpdateEvent
import com.gitlab.kordlib.core.event.WebhookUpdateEvent
import com.gitlab.kordlib.core.event.channel.*
import com.gitlab.kordlib.core.event.guild.*
import com.gitlab.kordlib.core.event.message.*
import com.gitlab.kordlib.core.event.role.RoleCreateEvent
import com.gitlab.kordlib.core.event.role.RoleDeleteEvent
import com.gitlab.kordlib.core.event.role.RoleUpdateEvent
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
suspend fun channelFor(event: Event): ChannelBehavior? {
    return when (event) {
        is ChannelCreateEvent -> event.channel
        is ChannelDeleteEvent -> event.channel
        is ChannelPinsUpdateEvent -> event.channel
        is ChannelUpdateEvent -> event.channel
        is InviteCreateEvent -> event.channel
        is InviteDeleteEvent -> event.channel
        is MessageCreateEvent -> event.message.channel
        is MessageUpdateEvent -> event.getMessage().channel  // TODO: Remove message get when Kord updates
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
suspend fun guildFor(event: Event): GuildBehavior? {
    return when (event) {
        is CategoryCreateEvent -> event.channel.guild
        is CategoryDeleteEvent -> event.channel.guild
        is CategoryUpdateEvent -> event.channel.guild
        is EmojisUpdateEvent -> event.guild
        is GuildCreateEvent -> event.guild
        is GuildDeleteEvent -> event.guild
        is GuildUpdateEvent -> event.guild
        is IntegrationsUpdateEvent -> event.guild
        is InviteCreateEvent -> event.guild
        is InviteDeleteEvent -> event.guild
        is MemberChunksEvent -> event.guild
        is MemberJoinEvent -> event.guild
        is MemberLeaveEvent -> event.guild
        is MemberUpdateEvent -> event.guild
        is MessageCreateEvent -> event.message.getGuild()
        is MessageDeleteEvent -> event.guild
        is MessageUpdateEvent -> event.getMessage().getGuild()
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
suspend fun memberFor(event: Event): MemberBehavior? {
    return when (event) {
        is MemberJoinEvent -> event.member
        is MemberUpdateEvent -> event.member
        is MessageCreateEvent -> event.message.getAuthorAsMember()
        is MessageDeleteEvent -> event.message?.getAuthorAsMember()
        is MessageUpdateEvent -> event.getMessage().getAuthorAsMember()
        is ReactionAddEvent -> event.getUserAsMember()
        is ReactionRemoveEvent -> event.getUserAsMember()
        is TypingStartEvent -> if (event.guildId != null) event.getGuild()!!.getMember(event.userId) else null

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
suspend fun messageFor(event: Event): MessageBehavior? {
    return when (event) {
        is MessageCreateEvent -> event.message
        is MessageDeleteEvent -> event.message
        is MessageUpdateEvent -> event.getMessage()  // TODO: Remove message get when Kord updates
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
fun roleFor(event: Event): RoleBehavior? {
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
suspend fun userFor(event: Event): UserBehavior? {
    return when (event) {
        is BanAddEvent -> event.user
        is BanRemoveEvent -> event.user

        // We don't deal with selfbots, so we only want the first user - bots can't be in group DMs.
        is DMChannelCreateEvent -> event.channel.recipients.first { it.id != event.kord.selfId }
        is DMChannelDeleteEvent -> event.channel.recipients.first { it.id != event.kord.selfId }
        is DMChannelUpdateEvent -> event.channel.recipients.first { it.id != event.kord.selfId }

        is MemberJoinEvent -> event.member
        is MemberLeaveEvent -> event.user
        is MemberUpdateEvent -> event.member
        is MessageCreateEvent -> event.message.author
        is MessageDeleteEvent -> event.message?.author
        is MessageUpdateEvent -> event.getMessage().author  // TODO: Remove message get when Kord updates
        is ReactionAddEvent -> event.user
        is ReactionRemoveEvent -> event.user
        is TypingStartEvent -> event.user
        is UserUpdateEvent -> event.user

        // TODO: This event doesn't yet have a User/UserBehavior for the change
        // is PresenceUpdateEvent -> event.user.

        else -> null
    }
}
