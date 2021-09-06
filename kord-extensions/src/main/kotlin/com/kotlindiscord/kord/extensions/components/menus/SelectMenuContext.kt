package com.kotlindiscord.kord.extensions.components.menus

import com.kotlindiscord.kord.extensions.components.Component
import com.kotlindiscord.kord.extensions.components.ComponentContext
import dev.kord.core.event.interaction.SelectMenuInteractionCreateEvent

public abstract class SelectMenuContext(
    component: Component,
    event: SelectMenuInteractionCreateEvent
) : ComponentContext<SelectMenuInteractionCreateEvent>(component, event) {
    public val selected: List<String> = event.interaction.values
}
