package com.kotlindiscord.kord.extensions.components.buttons

import com.kotlindiscord.kord.extensions.components.Component
import com.kotlindiscord.kord.extensions.components.ComponentContext
import dev.kord.core.event.interaction.ButtonInteractionCreateEvent

public abstract class InteractionButtonContext(
    component: Component,
    event: ButtonInteractionCreateEvent
) : ComponentContext<ButtonInteractionCreateEvent>(component, event)
