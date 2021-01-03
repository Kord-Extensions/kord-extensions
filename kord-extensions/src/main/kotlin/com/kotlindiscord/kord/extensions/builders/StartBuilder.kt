package com.kotlindiscord.kord.extensions.builders

import dev.kord.common.entity.PresenceStatus
import dev.kord.gateway.Intents
import dev.kord.gateway.builder.PresenceBuilder

/**
 * Builder class to make calling `bot.start()` with parameters a little less silly.
 *
 * Specifically, this allows it to take a single lambda instead of passing two lambdas, which was quite brittle
 * and unfriendly.
 */
public class StartBuilder {
    /** Presence builder function, defaulting to an online status with no activity. **/
    public var presenceBuilder: PresenceBuilder.() -> Unit = { status = PresenceStatus.Online }

    /** Intents builder function, defaulting to Kord's default intentions. **/
    public var intentsBuilder: (Intents.IntentsBuilder.() -> Unit)? = null

    /** DSL function allowing you to provide your own presence builder. **/
    public suspend fun presence(builder: PresenceBuilder.() -> Unit) {
        this.presenceBuilder = builder
    }

    /** DSL function allowing you to provide your own intents builder. **/
    public suspend fun intents(builder: Intents.IntentsBuilder.() -> Unit) {
        this.intentsBuilder = builder
    }
}
