package com.kotlindiscord.kord.extensions.events

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.extensions.Extension

/**
 * Event fired when an extension is unloaded programmatically.
 *
 * @param extension The extension that's just been unloaded.
 */
public class ExtensionUnloadedEvent(override val bot: ExtensibleBot, public val extension: Extension) : ExtensionEvent
