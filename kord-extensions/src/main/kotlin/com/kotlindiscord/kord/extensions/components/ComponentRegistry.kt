package com.kotlindiscord.kord.extensions.components

import com.kotlindiscord.kord.extensions.components.buttons.InteractionButtonWithAction
import com.kotlindiscord.kord.extensions.components.menus.SelectMenu
import dev.kord.core.event.interaction.ButtonInteractionCreateEvent
import dev.kord.core.event.interaction.SelectMenuInteractionCreateEvent
import mu.KLogger
import mu.KotlinLogging

/**
 * Component registry, keeps track of components and handles incoming interaction events, dispatching as needed to
 * registered component actions.
 */
public open class ComponentRegistry {
    internal val logger: KLogger = KotlinLogging.logger {}

    /** Map of registered component IDs to their components. **/
    public open val components: MutableMap<String, Component> = mutableMapOf()

    /** Register a component. Only components with IDs need registering. **/
    public open fun register(component: ComponentWithID) {
        logger.trace { "Registering component with ID: ${component.id}" }

        components[component.id] = component
    }

    /** Unregister a registered component. **/
    public open fun unregister(component: ComponentWithID): Component? =
        unregister(component.id)

    /** Unregister a registered component, by ID. **/
    public open fun unregister(id: String): Component? =
        components.remove(id)

    /** Dispatch a [ButtonInteractionCreateEvent] to its button component object. **/
    public suspend fun handle(event: ButtonInteractionCreateEvent) {
        val id = event.interaction.componentId

        when (val c = components[id]) {
            is InteractionButtonWithAction<*> -> c.call(event)

            null -> logger.warn { "Button interaction received for unknown component ID: $id" }

            else -> logger.warn {
                "Button interaction received for component ($id), but that component is not a button component with " +
                    "an action"
            }
        }
    }

    /** Dispatch a [SelectMenuInteractionCreateEvent] to its select (dropdown) menu component object. **/
    public suspend fun handle(event: SelectMenuInteractionCreateEvent) {
        val id = event.interaction.componentId

        when (val c = components[id]) {
            is SelectMenu<*> -> c.call(event)

            null -> logger.warn { "Select Menu interaction received for unknown component ID: $id" }

            else -> logger.warn {
                "Select Menu interaction received for component ($id), but that component is not a select menu"
            }
        }
    }
}
