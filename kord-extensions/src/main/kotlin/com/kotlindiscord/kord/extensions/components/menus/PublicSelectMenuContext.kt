package com.kotlindiscord.kord.extensions.components.menus

import com.kotlindiscord.kord.extensions.components.Component
import com.kotlindiscord.kord.extensions.interactions.PublicInteractionContext
import dev.kord.core.behavior.interaction.PublicInteractionResponseBehavior
import dev.kord.core.event.interaction.SelectMenuInteractionCreateEvent

public class PublicSelectMenuContext(
    override val component: Component,
    override val event: SelectMenuInteractionCreateEvent,
    override val interactionResponse: PublicInteractionResponseBehavior
) : SelectMenuContext(component, event), PublicInteractionContext
