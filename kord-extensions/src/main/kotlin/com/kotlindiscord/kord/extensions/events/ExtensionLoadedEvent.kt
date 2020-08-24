package com.kotlindiscord.kord.extensions.events

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.extensions.Extension

/**
 * Event fired when an extension is loaded, either on first load or programmatically later.
 *
 * @param extension The extension that's just been loaded.
 */
class ExtensionLoadedEvent(override val bot: ExtensibleBot, val extension: Extension) : ExtensionEvent
