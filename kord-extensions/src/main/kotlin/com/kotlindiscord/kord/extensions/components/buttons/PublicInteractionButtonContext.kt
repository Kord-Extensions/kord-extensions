package com.kotlindiscord.kord.extensions.components.buttons

import com.kotlindiscord.kord.extensions.interactions.PublicInteractionContext
import dev.kord.core.behavior.interaction.PublicInteractionResponseBehavior
import dev.kord.core.event.interaction.ButtonInteractionCreateEvent

public class PublicInteractionButtonContext(
    component: PublicInteractionButton,
    event: ButtonInteractionCreateEvent,
    override val interactionResponse: PublicInteractionResponseBehavior
) : InteractionButtonContext(component, event), PublicInteractionContext
