package com.kotlindiscord.kord.extensions.components.menus

import com.kotlindiscord.kord.extensions.components.Component
import com.kotlindiscord.kord.extensions.interactions.EphemeralInteractionContext
import dev.kord.core.behavior.interaction.EphemeralInteractionResponseBehavior
import dev.kord.core.event.interaction.SelectMenuInteractionCreateEvent

public class EphemeralSelectMenuContext(
    override val component: Component,
    override val event: SelectMenuInteractionCreateEvent,
    override val interactionResponse: EphemeralInteractionResponseBehavior
) : SelectMenuContext(component, event), EphemeralInteractionContext
