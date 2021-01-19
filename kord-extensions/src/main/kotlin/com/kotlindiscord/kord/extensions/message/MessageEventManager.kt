package com.kotlindiscord.kord.extensions.message

import com.kotlindiscord.kord.extensions.utils.waitFor
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.behavior.MessageBehavior
import dev.kord.core.entity.Message
import dev.kord.core.entity.ReactionEmoji
import dev.kord.core.event.Event
import dev.kord.core.event.channel.ChannelDeleteEvent
import dev.kord.core.event.guild.GuildDeleteEvent
import dev.kord.core.event.message.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Default event-listening timeout, 5 minutes.
 */
public const val DEFAULT_TIMEOUT: Long = 1000L * 60L * 5L

/**
 * Message event manager, in charge of registering (and dispatching to) message-specific event handlers.
 *
 * @property message Message to listen to events for.
 * @property guildId Message guild ID.
 * @property timeout How long to wait before cancelling listening, after the last relevant event.
 */
public open class MessageEventManager(
    public val message: Message,
    public val guildId: Snowflake?,
    private val timeout: Long? = DEFAULT_TIMEOUT
) {

    /**
     * Kord instance.
     */
    public val kord: Kord
        get() = message.kord

    /**
     * Event listener job.
     */
    public var job: Job? = null
        private set

    /**
     * `true` if we're currently listening for events, `false` otherwise.
     */
    @Volatile
    public var listening: Boolean = false
        private set

    /**
     * List of event handlers, by event type.
     */
    public val events: MutableList<suspend (Event) -> Unit> = mutableListOf()

    /**
     * Action to take when we stop listening for events.
     */
    public var stopAction: (suspend (MessageBehavior?) -> Unit)? = null

    /**
     * Specify a lambda (or function) to be called when we stop listening for events.
     *
     * If the message was deleted, the lambda will receive `null` as an argument - otherwise it'll get the
     * corresponding MessageBehavior.
     *
     * @param action Action to execute.
     */
    public fun stop(action: suspend (MessageBehavior?) -> Unit) {
        stopAction = action
    }

    /**
     * Listen for reaction-related events for the tracked message.
     *
     * @param emoji If specified, the emoji to match. Omit (or supply `null`) to match all emojis.
     * @param block Lambda (or function) to execute when a matching event is fired.
     *
     * @see [event]
     * @see [ReactionAddEvent]
     * @see [ReactionRemoveEvent]
     */
    public open fun reaction(emoji: ReactionEmoji? = null, block: suspend (Event) -> Unit) {
        event {
            if (it is ReactionAddEvent && (emoji == null || emoji == it.emoji) ||
                it is ReactionRemoveEvent && (emoji == null || emoji == it.emoji)
            ) {
                block(it)
            }
        }
    }

    /**
     * Listen for [ReactionAddEvent]s for the tracked message.
     *
     * @param emoji If specified, the emoji to match. Omit (or supply `null`) to match all emojis.
     * @param block Lambda (or function) to execute when a matching event is fired.
     *
     * @see [event]
     * @see [ReactionAddEvent]
     */
    public open fun reactionAdd(emoji: ReactionEmoji? = null, block: suspend (ReactionAddEvent) -> Unit) {
        event {
            if (it is ReactionAddEvent && (emoji == null || emoji == it.emoji)) {
                block(it)
            }
        }
    }

    /**
     * Listen for [ReactionRemoveEvent]s for the tracked message.
     *
     * @param emoji If specified, the emoji to match. Omit (or supply `null`) to match all emojis.
     * @param block Lambda (or function) to execute when a matching event is fired.
     *
     * @see [event]
     * @see [ReactionRemoveEvent]
     */
    public open fun reactionRemove(emoji: ReactionEmoji? = null, block: suspend (ReactionRemoveEvent) -> Unit) {
        event {
            if (it is ReactionRemoveEvent && (emoji == null || emoji == it.emoji)) {
                block(it)
            }
        }
    }

    /**
     * Listen for [ReactionRemoveAllEvent]s for the tracked message.
     *
     * @param block Lambda (or function) to execute when a matching event is fired.
     *
     * @see [event]
     * @see [ReactionRemoveAllEvent]
     */
    public open fun reactionRemoveAll(block: suspend (ReactionRemoveAllEvent) -> Unit) {
        event {
            if (it is ReactionRemoveAllEvent) {
                block(it)
            }
        }
    }

    /**
     * Listen for message-deletion events for the tracked message.
     *
     * This function will listen for single message deletes and bulk deletes concerning this message.
     *
     * @param block Lambda (or function) to execute when a matching event is fired.
     *
     * @see [event]
     */
    public open fun delete(block: suspend () -> Unit) {
        event {
            if (isDeleteEvent(it)) {
                block()
            }
        }
    }

    /**
     * Listen for [MessageBulkDeleteEvent]s for the tracked message.
     *
     * This function will not listen for single message deletes.
     *
     * @param block Lambda (or function) to execute when a matching event is fired.
     *
     * @see [event]
     * @see [MessageBulkDeleteEvent]
     */
    public open fun deleteBulk(block: suspend (MessageBulkDeleteEvent) -> Unit) {
        event {
            if (it is MessageBulkDeleteEvent) {
                block(it)
            }
        }
    }

    /**
     * Listen for [MessageDeleteEvent]s for the tracked message.
     *
     * This function will not listen for bulk deletes concerning this message.
     *
     * @param block Lambda (or function) to execute when a matching event is fired.
     *
     * @see [event]
     * @see [MessageDeleteEvent]
     */
    public open fun deleteOnly(block: suspend (MessageDeleteEvent) -> Unit) {
        event {
            if (it is MessageDeleteEvent) {
                block(it)
            }
        }
    }

    /**
     * Listen for [ChannelDeleteEvent]s for the tracked message.
     *
     * This function will listen for channel deletes where is the message is located.
     *
     * @param block Lambda (or function) to execute when a matching event is fired.
     *
     * @see [event]
     * @see [ChannelDeleteEvent]
     */
    public open fun deleteChannel(block: suspend (ChannelDeleteEvent) -> Unit) {
        event {
            if (it is ChannelDeleteEvent) {
                block(it)
            }
        }
    }

    /**
     * Listen for [GuildDeleteEvent]s for the tracked message.
     *
     * This function will listen for guild deletes where is the message is located.
     *
     * @param block Lambda (or function) to execute when a matching event is fired.
     *
     * @see [event]
     * @see [GuildDeleteEvent]
     */
    public open fun deleteGuild(block: suspend (GuildDeleteEvent) -> Unit) {
        event {
            if (it is GuildDeleteEvent) {
                block(it)
            }
        }
    }

    /**
     * Listen for message-updating events for the tracked message - for example, message edits.
     *
     * @param block Lambda (or function) to execute when a matching event is fired.
     *
     * @see [event]
     * @see [MessageUpdateEvent]
     */
    public open fun update(block: suspend (MessageUpdateEvent) -> Unit) {
        event {
            if (it is MessageUpdateEvent) {
                block(it)
            }
        }
    }

    /**
     * Listen for generic events concerning the tracked message.
     *
     * You can use this to do your own event matching, if needed.
     *
     * @param block Lambda (or function) to execute when a matching event is fired.
     *
     * @see [event]
     */
    public open fun event(block: suspend (Event) -> Unit) {
        events += block
    }

    /**
     * Start listening for events, if we aren't already listening.
     *
     * @return `true` if we started listening for events, `false` if we were already listening for events.
     */
    public open fun start(): Boolean {
        if (listening || job != null) return false

        job = kord.launch {
            val condition = getCheck()
            var wasDeleted = false

            listening = true

            while (isActive && listening && !wasDeleted) {
                val event = kord.waitFor(timeout, condition)

                if (event == null) {
                    listening = false
                } else {
                    events.forEach { it(event) }
                    wasDeleted = isDeleteEvent(event)
                }
            }

            stopListening(wasDeleted)
        }

        return true
    }

    /**
     * Check if the event is a deletion event.
     * @param event Event concerning the tracked message.
     *
     * @return `true` if the event concerns the deletion of the tracked message, `false` otherwise.
     *
     * @see [MessageDeleteEvent]
     * @see [MessageBulkDeleteEvent]
     * @see [ChannelDeleteEvent]
     * @see [GuildDeleteEvent]
     */
    private fun isDeleteEvent(event: Event): Boolean =
        event is MessageDeleteEvent ||
            event is MessageBulkDeleteEvent ||
            event is ChannelDeleteEvent ||
            event is GuildDeleteEvent

    /**
     * Stop listening for events, invoking the [stopAction] if one has been registered.
     *
     * @param messageDeleted `true` if the message was deleted, `false` otherwise.
     */
    private suspend fun stopListening(messageDeleted: Boolean) {
        listening = false

        stopAction?.invoke(if (messageDeleted) null else message)

        job?.cancel()
        job = null
    }

    /**
     * Create a lambda that returns `true` if the given [Event] is one that concerns the tracked message.
     *
     * @return Lambda that takes an [Event] object and returns a boolean representing whether the event concerns this
     * message.
     */
    protected open fun getCheck(): suspend (Event) -> Boolean = {
        val id = message.id
        when (it) {
            is ReactionAddEvent -> id == it.messageId && it.userId != kord.selfId
            is ReactionRemoveEvent -> id == it.messageId && it.userId != kord.selfId
            is ReactionRemoveAllEvent -> id == it.messageId
            is MessageDeleteEvent -> id == it.messageId
            is MessageUpdateEvent -> id == it.messageId
            is MessageBulkDeleteEvent -> id in it.messageIds
            is ChannelDeleteEvent -> it.channel.id == message.channelId
            is GuildDeleteEvent -> it.guildId == guildId

            else -> false
        }
    }
}
