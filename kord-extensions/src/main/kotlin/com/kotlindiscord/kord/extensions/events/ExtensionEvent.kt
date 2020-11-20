package com.kotlindiscord.kord.extensions.events

import com.kotlindiscord.kord.extensions.ExtensibleBot

/**
 * Base interface for events fired by Kord Extensions.
 */
public interface ExtensionEvent {
    /** Current bot instance for this event. **/
    public val bot: ExtensibleBot
}
