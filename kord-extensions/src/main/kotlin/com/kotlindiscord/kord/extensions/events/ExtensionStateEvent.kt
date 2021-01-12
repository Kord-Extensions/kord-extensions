package com.kotlindiscord.kord.extensions.events

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.ExtensionState

/**
 * Event fired when an extension's state changes.
 *
 * @property extension Extension that has a state change
 * @property state Extension's new state
 */
public class ExtensionStateEvent(
    override val bot: ExtensibleBot,
    public val extension: Extension,
    public val state: ExtensionState
) : ExtensionEvent
