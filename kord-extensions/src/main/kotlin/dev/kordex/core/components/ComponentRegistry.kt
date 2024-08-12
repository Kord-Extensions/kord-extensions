/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.components

import dev.kord.core.event.interaction.ButtonInteractionCreateEvent
import dev.kord.core.event.interaction.ModalSubmitInteractionCreateEvent
import dev.kord.core.event.interaction.SelectMenuInteractionCreateEvent
import dev.kordex.core.components.buttons.InteractionButtonWithAction
import dev.kordex.core.components.forms.ModalForm
import dev.kordex.core.components.menus.SelectMenu
import dev.kordex.core.registry.DefaultLocalRegistryStorage
import dev.kordex.core.registry.RegistryStorage
import dev.kordex.core.utils.scheduling.Scheduler
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging

/**
 * Component registry, keeps track of components and handles incoming interaction events, dispatching as needed to
 * registered component actions.
 */
public open class ComponentRegistry {
	internal val logger: KLogger = KotlinLogging.logger {}

	/** Scheduler that can be used to schedule timeout tasks. All [ComponentContainer] objects use this scheduler. **/
	public open val scheduler: Scheduler = Scheduler()

	/** Mapping of registered component IDs to their components. **/
	public open val storage: RegistryStorage<String, Component> = DefaultLocalRegistryStorage()

	/** Mapping of registered modal IDs to their form objects. **/
	public open val modalStorage: RegistryStorage<String, ModalForm> = DefaultLocalRegistryStorage()

	/** Register a component. Only components with IDs need registering. **/
	public open suspend fun register(component: ComponentWithID) {
		logger.trace { "Registering component with ID: ${component.id}" }

		storage.set(component.id, component)
	}

	/** Register a modal form. **/
	public open suspend fun register(modal: ModalForm) {
		logger.trace { "Registering modal with ID: ${modal.id}" }

		modalStorage.set(modal.id, modal)
	}

	/** Unregister a registered component. **/
	public open suspend fun unregister(component: ComponentWithID): Component? =
		unregister(component.id)

	/** Unregister a registered modal form. **/
	public open suspend fun unregisterModal(modal: ModalForm): ModalForm? =
		unregisterModal(modal.id)

	/** Unregister a registered component, by ID. **/
	public open suspend fun unregister(id: String): Component? =
		storage.remove(id)

	/** Unregister a registered modal form, by ID. **/
	public open suspend fun unregisterModal(id: String): ModalForm? =
		modalStorage.remove(id)

	/** Dispatch a [ModalSubmitInteractionCreateEvent] to its modal form object. **/
	public suspend fun handle(event: ModalSubmitInteractionCreateEvent) {
		val id = event.interaction.modalId
		val modal = modalStorage.get(id)

		if (modal == null) {
			logger.debug { "Modal interaction received for unknown modal ID: $id" }
		} else {
			modal.call(event)
		}
	}

	/** Dispatch a [ButtonInteractionCreateEvent] to its button component object. **/
	public suspend fun handle(event: ButtonInteractionCreateEvent) {
		val id = event.interaction.componentId

		when (val c = storage.get(id)) {
			is InteractionButtonWithAction<*, *> -> c.call(event)

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
			is SelectMenu<*, *> -> c.call(event)

			null -> logger.debug { "Select Menu interaction received for unknown component ID: $id" }

			else -> logger.warn {
				"Select Menu interaction received for component ($id), but that component is not a select menu"
			}
		}
	}
}
