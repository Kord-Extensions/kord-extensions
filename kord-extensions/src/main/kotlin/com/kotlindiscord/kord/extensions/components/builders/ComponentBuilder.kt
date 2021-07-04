@file:OptIn(KordPreview::class)

package com.kotlindiscord.kord.extensions.components.builders

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.commands.slash.SlashCommandContext
import com.kotlindiscord.kord.extensions.components.Components
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.sentry.SentryAdapter
import dev.kord.common.annotation.KordPreview
import dev.kord.core.Kord
import dev.kord.core.event.interaction.InteractionCreateEvent
import dev.kord.rest.builder.component.ActionRowBuilder
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Abstract class representing a button builder, providing common functionality and properties.
 */
public abstract class ComponentBuilder : KoinComponent {
    /** The [ExtensibleBot] instance that this extension is installed to. **/
    public val bot: ExtensibleBot by inject()

    /** Current Kord instance powering the bot. **/
    public val kord: Kord by inject()

    /** Sentry adapter, for easy access to Sentry functions. **/
    public val sentry: SentryAdapter by inject()

    /** Function used to add this button to an action row. **/
    public abstract fun apply(builder: ActionRowBuilder)

    /** Function called to validate the button. Should throw exceptions if something is invalid. **/
    public abstract fun validate()

    /**
     * For interactive button types, called in order to action the button. Throws [UnsupportedOperationException]
     * by default.
     */
    public open suspend fun call(
        components: Components,
        extension: Extension,
        event: InteractionCreateEvent,
        parentContext: SlashCommandContext<*>? = null
    ) {
        throw UnsupportedOperationException("This type of component doesn't support callable actions.")
    }
}
