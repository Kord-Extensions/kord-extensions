package com.kotlindiscord.kord.extensions.events

import dev.kord.core.Kord
import dev.kord.core.event.Event
import org.koin.core.component.KoinComponent
import kotlin.coroutines.CoroutineContext

/**
 * Base interface for events fired by Kord Extensions.
 */
public interface KordExEvent : Event, KoinComponent {
    override val kord: Kord get() = getKoin().get()
    override val shard: Int get() = -1
    override val coroutineContext: CoroutineContext get() = kord.coroutineContext
}
