package com.kotlindiscord.kord.extensions.components.buttons

import com.kotlindiscord.kord.extensions.types.EphemeralInteractionContext
import dev.kord.core.behavior.interaction.EphemeralInteractionResponseBehavior
import dev.kord.core.event.interaction.ButtonInteractionCreateEvent

/** Class representing the execution context for an ephemeral-only button. **/
public class EphemeralInteractionButtonContext(
    override val component: EphemeralInteractionButton,
    override val event: ButtonInteractionCreateEvent,
    override val interactionResponse: EphemeralInteractionResponseBehavior
) : InteractionButtonContext(component, event), EphemeralInteractionContext
