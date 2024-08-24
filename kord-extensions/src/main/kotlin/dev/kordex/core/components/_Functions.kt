/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.components

import dev.kord.rest.builder.message.MessageBuilder
import dev.kordex.core.components.buttons.DisabledInteractionButton
import dev.kordex.core.components.buttons.EphemeralInteractionButton
import dev.kordex.core.components.buttons.LinkInteractionButton
import dev.kordex.core.components.buttons.PublicInteractionButton
import dev.kordex.core.components.forms.ModalForm
import dev.kordex.core.components.menus.channel.EphemeralChannelSelectMenu
import dev.kordex.core.components.menus.channel.PublicChannelSelectMenu
import dev.kordex.core.components.menus.mentionable.EphemeralMentionableSelectMenu
import dev.kordex.core.components.menus.mentionable.PublicMentionableSelectMenu
import dev.kordex.core.components.menus.role.EphemeralRoleSelectMenu
import dev.kordex.core.components.menus.role.PublicRoleSelectMenu
import dev.kordex.core.components.menus.string.EphemeralStringSelectMenu
import dev.kordex.core.components.menus.string.PublicStringSelectMenu
import dev.kordex.core.components.menus.user.EphemeralUserSelectMenu
import dev.kordex.core.components.menus.user.PublicUserSelectMenu
import kotlin.time.Duration

// region: Buttons

/** DSL function for creating a disabled button and adding it to the current [ComponentContainer]. **/
public suspend fun ComponentContainer.disabledButton(
	row: Int? = null,
	builder: suspend DisabledInteractionButton.() -> Unit,
): DisabledInteractionButton {
	val component = DisabledInteractionButton()

	builder(component)
	add(component, row)

	return component
}

/** DSL function for creating an ephemeral button and adding it to the current [ComponentContainer]. **/
public suspend fun ComponentContainer.ephemeralButton(
	row: Int? = null,
	builder: suspend EphemeralInteractionButton<ModalForm>.() -> Unit,
): EphemeralInteractionButton<ModalForm> {
	val component = EphemeralInteractionButton<ModalForm>(timeoutTask)

	builder(component)
	add(component, row)

	return component
}

/** DSL function for creating an ephemeral button with modal, and adding it to the current [ComponentContainer]. **/
public suspend fun <M : ModalForm> ComponentContainer.ephemeralButton(
	modal: (() -> M)?,
	row: Int? = null,
	builder: suspend EphemeralInteractionButton<M>.() -> Unit,
): EphemeralInteractionButton<M> {
	val component = EphemeralInteractionButton(timeoutTask, modal)

	builder(component)
	add(component, row)

	return component
}

/** DSL function for creating a link button and adding it to the current [ComponentContainer]. **/
public suspend fun ComponentContainer.linkButton(
	row: Int? = null,
	builder: suspend LinkInteractionButton.() -> Unit,
): LinkInteractionButton {
	val component = LinkInteractionButton()

	builder(component)
	add(component, row)

	return component
}

/** DSL function for creating a public button and adding it to the current [ComponentContainer]. **/
public suspend fun ComponentContainer.publicButton(
	row: Int? = null,
	builder: suspend PublicInteractionButton<ModalForm>.() -> Unit,
): PublicInteractionButton<ModalForm> {
	val component = PublicInteractionButton<ModalForm>(timeoutTask)

	builder(component)
	add(component, row)

	return component
}

/** DSL function for creating a public button with modal, and adding it to the current [ComponentContainer]. **/
public suspend fun <M : ModalForm> ComponentContainer.publicButton(
	modal: (() -> M)?,
	row: Int? = null,
	builder: suspend PublicInteractionButton<M>.() -> Unit,
): PublicInteractionButton<M> {
	val component = PublicInteractionButton(timeoutTask, modal)

	builder(component)
	add(component, row)

	return component
}

// endregion

// region: Channel Select Menus

/** DSL function for creating an ephemeral channel select menu and adding it to the current [ComponentContainer]. **/
public suspend fun ComponentContainer.ephemeralChannelSelectMenu(
	row: Int? = null,
	builder: suspend EphemeralChannelSelectMenu<ModalForm>.() -> Unit,
): EphemeralChannelSelectMenu<ModalForm> {
	val component = EphemeralChannelSelectMenu<ModalForm>(timeoutTask)

	builder(component)
	add(component, row)

	return component
}

/** DSL function for creating an ephemeral channel select menu and adding it to the current [ComponentContainer]. **/
public suspend fun <M : ModalForm> ComponentContainer.ephemeralChannelSelectMenu(
	modal: (() -> M)?,
	row: Int? = null,
	builder: suspend EphemeralChannelSelectMenu<M>.() -> Unit,
): EphemeralChannelSelectMenu<M> {
	val component = EphemeralChannelSelectMenu<M>(timeoutTask, modal)

	builder(component)
	add(component, row)

	return component
}

/** DSL function for creating a public channel select menu and adding it to the current [ComponentContainer]. **/
public suspend fun ComponentContainer.publicChannelSelectMenu(
	row: Int? = null,
	builder: suspend PublicChannelSelectMenu<ModalForm>.() -> Unit,
): PublicChannelSelectMenu<ModalForm> {
	val component = PublicChannelSelectMenu<ModalForm>(timeoutTask)

	builder(component)
	add(component, row)

	return component
}

/** DSL function for creating a public channel select menu and adding it to the current [ComponentContainer]. **/
public suspend fun <M : ModalForm> ComponentContainer.publicChannelSelectMenu(
	modal: (() -> M)?,
	row: Int? = null,
	builder: suspend PublicChannelSelectMenu<M>.() -> Unit,
): PublicChannelSelectMenu<M> {
	val component = PublicChannelSelectMenu(timeoutTask, modal)

	builder(component)
	add(component, row)

	return component
}

// endregion

// region: Mentionable Select Menus

/**
 * DSL function for creating an ephemeral mentionable select menu,
 * and adding it to the current [ComponentContainer].
 **/
public suspend fun ComponentContainer.ephemeralMentionableSelectMenu(
	row: Int? = null,
	builder: suspend EphemeralMentionableSelectMenu<ModalForm>.() -> Unit,
): EphemeralMentionableSelectMenu<ModalForm> {
	val component = EphemeralMentionableSelectMenu<ModalForm>(timeoutTask)

	builder(component)
	add(component, row)

	return component
}

/**
 * DSL function for creating an ephemeral mentionable select menu,
 * and adding it to the current [ComponentContainer].
 **/
public suspend fun <M : ModalForm> ComponentContainer.ephemeralMentionableSelectMenu(
	modal: (() -> M)?,
	row: Int? = null,
	builder: suspend EphemeralMentionableSelectMenu<M>.() -> Unit,
): EphemeralMentionableSelectMenu<M> {
	val component = EphemeralMentionableSelectMenu<M>(timeoutTask, modal)

	builder(component)
	add(component, row)

	return component
}

/** DSL function for creating a public mentionable select menu and adding it to the current [ComponentContainer]. **/
public suspend fun ComponentContainer.publicMentionableSelectMenu(
	row: Int? = null,
	builder: suspend PublicMentionableSelectMenu<ModalForm>.() -> Unit,
): PublicMentionableSelectMenu<ModalForm> {
	val component = PublicMentionableSelectMenu<ModalForm>(timeoutTask)

	builder(component)
	add(component, row)

	return component
}

/** DSL function for creating a public mentionable select menu and adding it to the current [ComponentContainer]. **/
public suspend fun <M : ModalForm> ComponentContainer.publicMentionableSelectMenu(
	modal: (() -> M)?,
	row: Int? = null,
	builder: suspend PublicMentionableSelectMenu<M>.() -> Unit,
): PublicMentionableSelectMenu<M> {
	val component = PublicMentionableSelectMenu(timeoutTask, modal)

	builder(component)
	add(component, row)

	return component
}

// endregion

// region: Role Select Menus

/** DSL function for creating an ephemeral role select menu and adding it to the current [ComponentContainer]. **/
public suspend fun ComponentContainer.ephemeralRoleSelectMenu(
	row: Int? = null,
	builder: suspend EphemeralRoleSelectMenu<ModalForm>.() -> Unit,
): EphemeralRoleSelectMenu<ModalForm> {
	val component = EphemeralRoleSelectMenu<ModalForm>(timeoutTask)

	builder(component)
	add(component, row)

	return component
}

/** DSL function for creating an ephemeral role select menu and adding it to the current [ComponentContainer]. **/
public suspend fun <M : ModalForm> ComponentContainer.ephemeralRoleSelectMenu(
	modal: (() -> M)?,
	row: Int? = null,
	builder: suspend EphemeralRoleSelectMenu<M>.() -> Unit,
): EphemeralRoleSelectMenu<M> {
	val component = EphemeralRoleSelectMenu<M>(timeoutTask, modal)

	builder(component)
	add(component, row)

	return component
}

/** DSL function for creating a public role select menu and adding it to the current [ComponentContainer]. **/
public suspend fun ComponentContainer.publicRoleSelectMenu(
	row: Int? = null,
	builder: suspend PublicRoleSelectMenu<ModalForm>.() -> Unit,
): PublicRoleSelectMenu<ModalForm> {
	val component = PublicRoleSelectMenu<ModalForm>(timeoutTask)

	builder(component)
	add(component, row)

	return component
}

/** DSL function for creating a public role select menu and adding it to the current [ComponentContainer]. **/
public suspend fun <M : ModalForm> ComponentContainer.publicRoleSelectMenu(
	modal: (() -> M)?,
	row: Int? = null,
	builder: suspend PublicRoleSelectMenu<M>.() -> Unit,
): PublicRoleSelectMenu<M> {
	val component = PublicRoleSelectMenu(timeoutTask, modal)

	builder(component)
	add(component, row)

	return component
}

// endregion

// region: String Select Menus

/** DSL function for creating an ephemeral string select menu and adding it to the current [ComponentContainer]. **/
public suspend fun ComponentContainer.ephemeralStringSelectMenu(
	row: Int? = null,
	builder: suspend EphemeralStringSelectMenu<ModalForm>.() -> Unit,
): EphemeralStringSelectMenu<ModalForm> {
	val component = EphemeralStringSelectMenu<ModalForm>(timeoutTask)

	builder(component)
	add(component, row)

	return component
}

/** DSL function for creating an ephemeral string select menu and adding it to the current [ComponentContainer]. **/
public suspend fun <M : ModalForm> ComponentContainer.ephemeralStringSelectMenu(
	modal: (() -> M)?,
	row: Int? = null,
	builder: suspend EphemeralStringSelectMenu<M>.() -> Unit,
): EphemeralStringSelectMenu<M> {
	val component = EphemeralStringSelectMenu(timeoutTask, modal)

	builder(component)
	add(component, row)

	return component
}

/** DSL function for creating a public string select menu and adding it to the current [ComponentContainer]. **/
public suspend fun ComponentContainer.publicStringSelectMenu(
	row: Int? = null,
	builder: suspend PublicStringSelectMenu<ModalForm>.() -> Unit,
): PublicStringSelectMenu<ModalForm> {
	val component = PublicStringSelectMenu<ModalForm>(timeoutTask)

	builder(component)
	add(component, row)

	return component
}

/** DSL function for creating a public string select menu and adding it to the current [ComponentContainer]. **/
public suspend fun <M : ModalForm> ComponentContainer.publicStringSelectMenu(
	modal: (() -> M)?,
	row: Int? = null,
	builder: suspend PublicStringSelectMenu<M>.() -> Unit,
): PublicStringSelectMenu<M> {
	val component = PublicStringSelectMenu(timeoutTask, modal)

	builder(component)
	add(component, row)

	return component
}

// endregion

// region: User Select Menus

/** DSL function for creating an ephemeral user select menu and adding it to the current [ComponentContainer]. **/
public suspend fun ComponentContainer.ephemeralUserSelectMenu(
	row: Int? = null,
	builder: suspend EphemeralUserSelectMenu<ModalForm>.() -> Unit,
): EphemeralUserSelectMenu<ModalForm> {
	val component = EphemeralUserSelectMenu<ModalForm>(timeoutTask)

	builder(component)
	add(component, row)

	return component
}

/** DSL function for creating an ephemeral user select menu and adding it to the current [ComponentContainer]. **/
public suspend fun <M : ModalForm> ComponentContainer.ephemeralUserSelectMenu(
	modal: (() -> M)?,
	row: Int? = null,
	builder: suspend EphemeralUserSelectMenu<M>.() -> Unit,
): EphemeralUserSelectMenu<M> {
	val component = EphemeralUserSelectMenu<M>(timeoutTask, modal)

	builder(component)
	add(component, row)

	return component
}

/** DSL function for creating a public user select menu and adding it to the current [ComponentContainer]. **/
public suspend fun ComponentContainer.publicUserSelectMenu(
	row: Int? = null,
	builder: suspend PublicUserSelectMenu<ModalForm>.() -> Unit,
): PublicUserSelectMenu<ModalForm> {
	val component = PublicUserSelectMenu<ModalForm>(timeoutTask)

	builder(component)
	add(component, row)

	return component
}

/** DSL function for creating a public user select menu and adding it to the current [ComponentContainer]. **/
public suspend fun <M : ModalForm> ComponentContainer.publicUserSelectMenu(
	modal: (() -> M)?,
	row: Int? = null,
	builder: suspend PublicUserSelectMenu<M>.() -> Unit,
): PublicUserSelectMenu<M> {
	val component = PublicUserSelectMenu(timeoutTask, modal)

	builder(component)
	add(component, row)

	return component
}

// endregion

// region: Utility functions

/** Convenience function for applying the components in a [ComponentContainer] to a message you're creating. **/
public suspend fun MessageBuilder.applyComponents(components: ComponentContainer) {
	with(components) {
		applyToMessage()
	}
}

/**
 * Convenience function for creating a [ComponentContainer] and components, and applying it to a message you're
 * creating. Supply a [timeout] and the components you add will be removed from the registry after the given period
 * of inactivity.
 */
public suspend fun MessageBuilder.components(
	timeout: Duration? = null,
	builder: suspend ComponentContainer.() -> Unit,
): ComponentContainer {
	val container = ComponentContainer(timeout, true, builder)

	applyComponents(container)

	return container
}

// endregion
