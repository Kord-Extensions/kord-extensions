@file:OptIn(KordPreview::class, KordUnsafe::class, KordExperimental::class)

package com.kotlindiscord.kord.extensions.checks

import com.kotlindiscord.kord.extensions.checks.types.CheckContext
import com.kotlindiscord.kord.extensions.utils.authorId
import dev.kord.common.annotation.KordExperimental
import dev.kord.common.annotation.KordPreview
import dev.kord.common.annotation.KordUnsafe
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.*
import dev.kord.core.behavior.channel.ChannelBehavior
import dev.kord.core.behavior.channel.threads.ThreadChannelBehavior
import dev.kord.core.cache.data.toData
import dev.kord.core.entity.Member
import dev.kord.core.entity.interaction.GuildApplicationCommandInteraction
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
        is WebhookUpdateEvent -> event.channel

        is ThreadChannelCreateEvent -> event.channel
        is ThreadUpdateEvent -> event.channel
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
        is WebhookUpdateEvent -> event.channel.id.value

        is ThreadChannelCreateEvent -> event.channel.id.value
        is ThreadUpdateEvent -> event.channel.id.value
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
        is WebhookUpdateEvent -> event.channel.id

        is ThreadChannelCreateEvent -> event.channel.id
        is ThreadUpdateEvent -> event.channel.id
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

            if (guildId == null) {
                null
            } else {
                event.kord.unsafe.guild(guildId)
            }
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

            if (guildId == null) {
                guildId
            } else {
                event.kord.unsafe.guild(guildId)
            }
        }

        is MessageDeleteEvent -> event.guild

        is MessageUpdateEvent -> {
            val guildId = event.new.guildId.value

            if (guildId == null) {
                guildId
            } else {
                event.kord.unsafe.guild(guildId)
            }
        }

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
    return when {
        event is InteractionCreateEvent -> (event.interaction as? GuildApplicationCommandInteraction)?.member

        event is MemberJoinEvent -> event.member
        event is MemberUpdateEvent -> event.member
        event is MessageCreateEvent -> event.member
        event is MessageDeleteEvent -> event.message?.data?.guildId?.value
            ?.let { event.kord.unsafe.member(it, event.message!!.data.authorId) }

        event is MessageUpdateEvent -> {
            val message = event.new
            if (message.author.value != null && message.member.value != null) {
                val userData = message.author.value!!.toData()
                val memberData = message.member.value!!.toData(userData.id, event.new.guildId.value!!)
                return Member(memberData, userData, event.kord)
            }
            return null
        }
        event is ReactionAddEvent -> event.userAsMember
        event is ReactionRemoveEvent -> event.userAsMember

        event is TypingStartEvent -> if (event.guildId != null) {
            event.getGuild()!!.getMemberOrNull(event.userId)
        } else {
            null
        }

        event is ThreadChannelCreateEvent -> event.channel.owner.asMember(event.channel.guildId)
//        event is ThreadUpdateEvent -> event.
//        event is ThreadChannelDeleteEvent -> event.
//        event is ThreadListSyncEvent -> event.

        event is ThreadMemberUpdateEvent -> {
            val thread = event.member.getThreadOrNull()

            if (thread == null) {
                null
            } else {
                event.member.asMember(thread.guildId)
            }
        }

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
