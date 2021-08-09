package com.kotlindiscord.kord.extensions.events

import com.kotlindiscord.kord.extensions.ExtensibleBot
import dev.kord.core.Kord
import dev.kord.core.event.Event
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Base interface for events fired by Kord Extensions.
 */
public abstract class ExtensionEvent : Event, KoinComponent {
    /** Current bot instance for this event. **/
    public val bot: ExtensibleBot by inject()

    override val kord: Kord by inject()
    override val shard: Int = -1
}
