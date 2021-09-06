package com.kotlindiscord.kord.extensions.components

import com.kotlindiscord.kord.extensions.components.buttons.InteractionButtonWithAction
import com.kotlindiscord.kord.extensions.components.menus.SelectMenu
import dev.kord.core.event.interaction.ButtonInteractionCreateEvent
import dev.kord.core.event.interaction.SelectMenuInteractionCreateEvent
import mu.KLogger
import mu.KotlinLogging

public open class ComponentRegistry {
    internal val logger: KLogger = KotlinLogging.logger {}

    public open val components: MutableMap<String, Component> = mutableMapOf()

    public open fun register(component: ComponentWithID) {
        logger.debug { "Registering component with ID: ${component.id}" }

        components[component.id] = component
    }

    public open fun unregister(component: ComponentWithID): Component? =
        unregister(component.id)

    public open fun unregister(id: String): Component? {
        return components.remove(id)
    }

    public suspend fun handle(event: ButtonInteractionCreateEvent) {
        val id = event.interaction.componentId

        when (val c = components[id]) {
            is InteractionButtonWithAction<*> -> { c.call(event) }

            null -> logger.warn { "Button interaction received for unknown component ID: $id" }

            else -> logger.warn {
                "Button interaction received for component ($id), but that component is not a button component with " +
                    "an action"
            }
        }
    }

    public suspend fun handle(event: SelectMenuInteractionCreateEvent) {
        val id = event.interaction.componentId

        when (val c = components[id]) {
            is SelectMenu<*> -> { c.call(event) }

            null -> logger.warn { "Select Menu interaction received for unknown component ID: $id" }

            else -> logger.warn {
                "Select Menu interaction received for component ($id), but that component is not a select menu"
            }
        }
    }
}
