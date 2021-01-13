package com.kotlindiscord.kord.extensions.message

import com.kotlindiscord.kord.extensions.utils.events
import com.kotlindiscord.kord.extensions.utils.waitFor
import dev.kord.core.Kord
import dev.kord.core.behavior.MessageBehavior
import dev.kord.core.entity.Message
import dev.kord.core.entity.ReactionEmoji
import dev.kord.core.event.Event
import dev.kord.core.event.message.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * 5 minutes, default time to wait for message events before stopping.
 */
public const val DEFAULT_TIME_LISTENING: Long = 1000L * 60L * 5L

/**
 * Manager of listeners about the events for a message.
 * @property message Message responsible of events.
 * @property timeout Time to stop listening to events after the last received event corresponding to the message.
 */
public open class MessageEventManager(
    public val message: MessageBehavior,
    private val timeout: Long? = DEFAULT_TIME_LISTENING
) {

    /**
     * Kord instance.
     */
    public val kord: Kord
        get() = message.kord

    /**
     * Job running to listen to events.
     */
    public var job: Job? = null
        private set

    /**
     * `true` if the manager listening the events, `false` otherwise.
     */
    @Volatile
    public var listening: Boolean = false
        private set

    /**
     * List of events that will be apply according to the type of a event.
     */
    public val events: MutableList<suspend (Event) -> Unit> = mutableListOf()

    /**
     * Action to apply to the end of the listening.
     */
    public var stopAction: (suspend (MessageBehavior?) -> Unit)? = null

    init {
        addDefaultRemoveAllReactionEvent()
    }

    /**
     * Add the default event when all reaction are removed to stop the listening.
     * @see [reactionRemoveAll]
     * @see [stopListening]
     */
    protected open fun addDefaultRemoveAllReactionEvent() {
        reactionRemoveAll { stopListening() }
    }

    /**
     * Execute a block of code when the listening is ended.
     * @param action Action to execute.
     */
    public fun stop(action: suspend (MessageBehavior?) -> Unit) {
        stopAction = action
    }

    /**
     * Listen an event about the reaction on [message].
     * @param emoji Emoji which the listen is applied.
     * If [emoji] is `null` then listen all emojis.
     * @param block Action to execute when event about a reaction for [message] is called.
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
     * Listen an event when a reaction is added on [message].
     * @param emoji Emoji which the listen is applied.
     * If [emoji] is `null` then listen all emojis.
     * @param block Action to execute when event about a reaction added for [message] is called.
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
     * Listen an event when a reaction is removed on [message].
     * @param emoji Emoji which the listen is applied.
     * If [emoji] is `null` then listen all emojis.
     * @param block Action to execute when event about a reaction removed for [message] is called.
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
     * Listen an event when all reactions are removed on [message].
     * @param block Action to execute when event about the suppression of all reaction for [message] is called.
     */
    public open fun reactionRemoveAll(block: suspend (ReactionRemoveAllEvent) -> Unit) {
        event {
            if (it is ReactionRemoveAllEvent) {
                block(it)
            }
        }
    }

    /**
     * Apply an action when the [message] is deleted by a selected delete or a global delete.
     * @param block Action to execute.
     * @see [event]
     * @see [MessageDeleteEvent]
     * @see [MessageBulkDeleteEvent]
     */
    public open fun delete(block: suspend (MessageDeleteEvent) -> Unit) {
        event {
            val deleteEvent = when (it) {
                is MessageDeleteEvent -> it

                is MessageBulkDeleteEvent -> {
                    val msg = if (message is Message) message else null
                    MessageDeleteEvent(message.id, it.channelId, it.guildId, msg, it.kord, it.shard, it.supplier)
                }

                else -> return@event
            }

            block(deleteEvent)
        }
    }

    /**
     * Apply an action when the [message] is deleted by a global delete.
     * @param block Action to execute.
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
     * Apply an action when the [message] is deleted by a selected delete.
     * @param block Action to execute.
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
     * Apply an action when the [message] is updated.
     * @param action Action to execute.
     * @see [event]
     * @see [MessageUpdateEvent]
     */
    public open fun update(action: suspend (MessageUpdateEvent) -> Unit) {
        event {
            if (it is MessageUpdateEvent) {
                action(it)
            }
        }
    }

    /**
     * Add a new action to apply when the [message] create an event.
     * and execute the listening of events.
     * @param block Code to execute when any event is receive about [message].
     * @see [events]
     */
    public open fun event(block: suspend (Event) -> Unit) {
        events += block
    }

    /**
     * Start the listening if necessary.
     * @return `true` if the listening start, `false` if he is already started
     */
    public open fun start(): Boolean {
        if (listening || job != null) return false

        job = kord.launch {
            val condition = getCondition()
            var isDelete = false

            listening = true

            while (isActive && listening && !isDelete) {
                val event = kord.waitFor(timeout, condition)

                if (event == null) {
                    listening = false
                } else {
                    events.forEach { it(event) }
                    isDelete = event is MessageDeleteEvent || event is MessageBulkDeleteEvent
                }
            }

            endListeningAction(isDelete)
        }

        return true
    }

    /**
     * Apply specific action when the listening is ended.
     * @param messageDeleted `true` if the message is deleted by an event, `false` otherwise.
     */
    private suspend fun endListeningAction(messageDeleted: Boolean) {
        listening = false
        stopAction?.invoke(if (messageDeleted) null else message)
        job = null
    }

    /**
     * Stop the listening.
     */
    public open fun stopListening() {
        listening = false
    }

    /**
     * Get the function to know if an event will be accepted in the processing.
     * @return Function of condition to accept or not the event received.
     */
    protected open fun getCondition(): suspend (Event) -> Boolean = {
        val id = message.id
        when (it) {
            is ReactionAddEvent -> id == it.messageId && it.userId != kord.selfId
            is ReactionRemoveEvent -> id == it.messageId && it.userId != kord.selfId
            is ReactionRemoveAllEvent -> id == it.messageId
            is MessageDeleteEvent -> id == it.messageId
            is MessageUpdateEvent -> id == it.messageId
            is MessageBulkDeleteEvent -> id in it.messageIds
            else -> false
        }
    }
}
