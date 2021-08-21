package com.kotlindiscord.kord.extensions.components.contexts

import com.kotlindiscord.kord.extensions.annotations.ExtensionDSL
import com.kotlindiscord.kord.extensions.components.Components
import com.kotlindiscord.kord.extensions.extensions.Extension
import dev.kord.common.annotation.KordPreview
import dev.kord.core.behavior.interaction.*
import dev.kord.core.entity.interaction.*
import dev.kord.core.event.interaction.ComponentCreateEvent
import org.koin.core.component.KoinComponent

/**
 * Context object representing the execution context of an interactive button interaction.
 */
@OptIn(KordPreview::class)
@ExtensionDSL
public open class InteractiveButtonContext(
    extension: Extension,
    event: ComponentCreateEvent,
    components: Components,
    interactionResponse: InteractionResponseBehavior? = null,
    interaction: ButtonInteraction = event.interaction as ButtonInteraction
) : KoinComponent, ActionableComponentContext<ButtonInteraction>(
    extension, event, components, interactionResponse, interaction
)
