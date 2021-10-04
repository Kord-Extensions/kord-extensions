package com.kotlindiscord.kord.extensions.components

import com.kotlindiscord.kord.extensions.components.buttons.InteractionButtonWithAction
import com.kotlindiscord.kord.extensions.components.menus.SelectMenu
import com.kotlindiscord.kord.extensions.registry.DefaultLocalRegistryStorage
import com.kotlindiscord.kord.extensions.registry.RegistryStorage
import com.kotlindiscord.kord.extensions.utils.scheduling.Scheduler
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

    /** Scheduler that can be used to schedule timeout tasks. All [ComponentContainer] objects use this scheduler. **/
    public open val scheduler: Scheduler = Scheduler()

    /** Map of registered component IDs to their components. **/
    public open val storage: RegistryStorage<String, Component> = DefaultLocalRegistryStorage()

    /** Register a component. Only components with IDs need registering. **/
    public open suspend fun register(component: ComponentWithID) {
        logger.trace { "Registering component with ID: ${component.id}" }

        storage.set(component.id, component)
    }

    /** Unregister a registered component. **/
    public open suspend fun unregister(component: ComponentWithID): Component? =
        unregister(component.id)

    /** Unregister a registered component, by ID. **/
    public open suspend fun unregister(id: String): Component? =
        storage.remove(id)

    /** Dispatch a [ButtonInteractionCreateEvent] to its button component object. **/
    public suspend fun handle(event: ButtonInteractionCreateEvent) {
        val id = event.interaction.componentId

        when (val c = storage.get(id)) {
            is InteractionButtonWithAction<*> -> c.call(event)

            null -> logger.debug { "Button interaction received for unknown component ID: $id" }

            else -> logger.warn {
                "Button interaction received for component ($id), but that component is not a button component with " +
                    "an action"
            }
        }
    }

    /** Dispatch a [SelectMenuInteractionCreateEvent] to its select (dropdown) menu component object. **/
    public suspend fun handle(event: SelectMenuInteractionCreateEvent) {
        val id = event.interaction.componentId

        when (val c = storage.get(id)) {
            is SelectMenu<*> -> c.call(event)

            null -> logger.debug { "Select Menu interaction received for unknown component ID: $id" }

            else -> logger.warn {
                "Select Menu interaction received for component ($id), but that component is not a select menu"
            }
        }
    }
}
