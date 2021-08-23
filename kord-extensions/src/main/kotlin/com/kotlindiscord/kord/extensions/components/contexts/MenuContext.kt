package com.kotlindiscord.kord.extensions.components.contexts

import com.kotlindiscord.kord.extensions.annotations.ExtensionDSL
import com.kotlindiscord.kord.extensions.components.Components
import com.kotlindiscord.kord.extensions.extensions.Extension
import dev.kord.common.annotation.KordPreview
import dev.kord.core.behavior.interaction.*
import dev.kord.core.entity.interaction.SelectMenuInteraction
import dev.kord.core.event.interaction.ComponentInteractionCreateEvent
import org.koin.core.component.KoinComponent
import java.util.*

/**
 * Context object representing the execution context of a menu selection interaction.
 */
@OptIn(KordPreview::class)
@ExtensionDSL
public open class MenuContext(
    extension: Extension,
    event: ComponentInteractionCreateEvent,
    components: Components,
    interactionResponse: InteractionResponseBehavior? = null,
    interaction: SelectMenuInteraction = event.interaction as SelectMenuInteraction
) : KoinComponent, ActionableComponentContext<SelectMenuInteraction>(
    extension, event, components, interactionResponse, interaction
) {
    /** Quick access to the selected values. **/
    public val selected: List<String> = interaction.values
}
