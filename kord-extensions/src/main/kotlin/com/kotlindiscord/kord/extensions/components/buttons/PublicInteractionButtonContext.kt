package com.kotlindiscord.kord.extensions.components.buttons

import com.kotlindiscord.kord.extensions.types.PublicInteractionContext
import dev.kord.core.behavior.interaction.PublicInteractionResponseBehavior
import dev.kord.core.event.interaction.ButtonInteractionCreateEvent

/** Class representing the execution context for a public-only button. **/
public class PublicInteractionButtonContext(
    component: PublicInteractionButton,
    event: ButtonInteractionCreateEvent,
    override val interactionResponse: PublicInteractionResponseBehavior
) : InteractionButtonContext(component, event), PublicInteractionContext
