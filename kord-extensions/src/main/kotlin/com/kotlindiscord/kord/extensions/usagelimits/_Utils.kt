package com.kotlindiscord.kord.extensions.usagelimits

import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.event.Event
import dev.kord.core.event.interaction.ApplicationCommandInteractionCreateEvent
import dev.kord.core.event.message.MessageCreateEvent
import kotlinx.datetime.Instant
import java.util.*

internal fun LinkedList<Instant>.removeSmaller(cutoffTime: Instant) {
    val iterator = this.iterator()

    while (iterator.hasNext()) {
        if (iterator.next() < cutoffTime) {
            iterator.remove()
        } else {
            break
        }
    }
}

/** Send an (ephemeral if possible) message based on the type of event. **/
internal suspend fun Event.sendEphemeralMessage(message: String) {
    when (this) {
        is MessageCreateEvent -> this.message.channel.createMessage(message)

        is ApplicationCommandInteractionCreateEvent -> this.interaction.respondEphemeral {
            this.content = message
        }

        else -> error("Unknown event type: $this")
    }
}
