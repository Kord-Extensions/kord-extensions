package com.kotlindiscord.kord.extensions.components.buttons

import com.kotlindiscord.kord.extensions.interactions.EphemeralInteractionContext
import dev.kord.core.behavior.interaction.EphemeralInteractionResponseBehavior
import dev.kord.core.event.interaction.ButtonInteractionCreateEvent

public class EphemeralInteractionButtonContext(
    override val component: EphemeralInteractionButton,
    override val event: ButtonInteractionCreateEvent,
    override val interactionResponse: EphemeralInteractionResponseBehavior
) : InteractionButtonContext(component, event), EphemeralInteractionContext
