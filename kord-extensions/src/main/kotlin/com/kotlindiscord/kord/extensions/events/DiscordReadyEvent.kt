package com.kotlindiscord.kord.extensions.events

import com.kotlindiscord.kord.extensions.ExtensibleBot

/**
 * This event is sent once all gateways have sent the ready event.
 */
public data class DiscordReadyEvent(override val bot: ExtensibleBot) : ExtensionEvent
