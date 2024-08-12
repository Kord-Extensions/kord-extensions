/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.modules.dev.unsafe.extensions

import dev.kordex.core.components.ComponentContainer
import dev.kordex.modules.dev.unsafe.annotations.UnsafeAPI
import dev.kordex.modules.dev.unsafe.components.buttons.UnsafeInteractionButton
import dev.kordex.modules.dev.unsafe.components.forms.UnsafeModalForm
import dev.kordex.modules.dev.unsafe.components.menus.channel.UnsafeChannelSelectMenu
import dev.kordex.modules.dev.unsafe.components.menus.mentionable.UnsafeMentionableSelectMenu
import dev.kordex.modules.dev.unsafe.components.menus.role.UnsafeRoleSelectMenu
import dev.kordex.modules.dev.unsafe.components.menus.string.UnsafeStringSelectMenu
import dev.kordex.modules.dev.unsafe.components.menus.user.UnsafeUserSelectMenu

// region: Buttons

/** DSL function for creating an unsafe button and adding it to the current [ComponentContainer]. **/
@UnsafeAPI
public suspend fun ComponentContainer.unsafeButton(
	row: Int? = null,
	builder: suspend UnsafeInteractionButton<UnsafeModalForm>.() -> Unit,
): UnsafeInteractionButton<UnsafeModalForm> {
	val component = UnsafeInteractionButton<UnsafeModalForm>(timeoutTask)

	builder(component)
	add(component, row)

	return component
}

/** DSL function for creating an unsafe button with modal, and adding it to the current [ComponentContainer]. **/
@UnsafeAPI
public suspend fun <M : UnsafeModalForm> ComponentContainer.unsafeButton(
	modal: (() -> M)?,
	row: Int? = null,
	builder: suspend UnsafeInteractionButton<M>.() -> Unit,
): UnsafeInteractionButton<M> {
	val component = UnsafeInteractionButton<M>(timeoutTask, modal)

	builder(component)
	add(component, row)

	return component
}

// endregion

// region: Channel Select Menus

@UnsafeAPI
public suspend fun ComponentContainer.unsafeChannelSelectMenu(
	row: Int? = null,
	builder: suspend UnsafeChannelSelectMenu<UnsafeModalForm>.() -> Unit,
): UnsafeChannelSelectMenu<UnsafeModalForm> {
	val component = UnsafeChannelSelectMenu<UnsafeModalForm>(timeoutTask)

	builder(component)
	add(component, row)

	return component
}

@UnsafeAPI
public suspend fun <M : UnsafeModalForm>  ComponentContainer.unsafeChannelSelectMenu(
	modal: (() -> M)?,
	row: Int? = null,
	builder: suspend UnsafeChannelSelectMenu<M>.() -> Unit,
): UnsafeChannelSelectMenu<M> {
	val component = UnsafeChannelSelectMenu(timeoutTask, modal)

	builder(component)
	add(component, row)

	return component
}

// endregion

// region: Mentionable Select Menus

@UnsafeAPI
public suspend fun ComponentContainer.unsafeMentionableSelectMenu(
	row: Int? = null,
	builder: suspend UnsafeMentionableSelectMenu<UnsafeModalForm>.() -> Unit,
): UnsafeMentionableSelectMenu<UnsafeModalForm> {
	val component = UnsafeMentionableSelectMenu<UnsafeModalForm>(timeoutTask)

	builder(component)
	add(component, row)

	return component
}

@UnsafeAPI
public suspend fun <M : UnsafeModalForm>  ComponentContainer.unsafeMentionableSelectMenu(
	modal: (() -> M)?,
	row: Int? = null,
	builder: suspend UnsafeMentionableSelectMenu<M>.() -> Unit,
): UnsafeMentionableSelectMenu<M> {
	val component = UnsafeMentionableSelectMenu(timeoutTask, modal)

	builder(component)
	add(component, row)

	return component
}

// endregion

// region: Role Select Menus

@UnsafeAPI
public suspend fun ComponentContainer.unsafeRoleSelectMenu(
	row: Int? = null,
	builder: suspend UnsafeRoleSelectMenu<UnsafeModalForm>.() -> Unit,
): UnsafeRoleSelectMenu<UnsafeModalForm> {
	val component = UnsafeRoleSelectMenu<UnsafeModalForm>(timeoutTask)

	builder(component)
	add(component, row)

	return component
}

@UnsafeAPI
public suspend fun <M : UnsafeModalForm>  ComponentContainer.unsafeRoleSelectMenu(
	modal: (() -> M)?,
	row: Int? = null,
	builder: suspend UnsafeRoleSelectMenu<M>.() -> Unit,
): UnsafeRoleSelectMenu<M> {
	val component = UnsafeRoleSelectMenu(timeoutTask, modal)

	builder(component)
	add(component, row)

	return component
}

// endregion

// region: String Select Menus

@UnsafeAPI
public suspend fun ComponentContainer.unsafeStringSelectMenu(
	row: Int? = null,
	builder: suspend UnsafeStringSelectMenu<UnsafeModalForm>.() -> Unit,
): UnsafeStringSelectMenu<UnsafeModalForm> {
	val component = UnsafeStringSelectMenu<UnsafeModalForm>(timeoutTask)

	builder(component)
	add(component, row)

	return component
}

@UnsafeAPI
public suspend fun <M : UnsafeModalForm>  ComponentContainer.unsafeStringSelectMenu(
	modal: (() -> M)?,
	row: Int? = null,
	builder: suspend UnsafeStringSelectMenu<M>.() -> Unit,
): UnsafeStringSelectMenu<M> {
	val component = UnsafeStringSelectMenu(timeoutTask, modal)

	builder(component)
	add(component, row)

	return component
}

// endregion

// region: User Select Menus

@UnsafeAPI
public suspend fun ComponentContainer.unsafeUserSelectMenu(
	row: Int? = null,
	builder: suspend UnsafeUserSelectMenu<UnsafeModalForm>.() -> Unit,
): UnsafeUserSelectMenu<UnsafeModalForm> {
	val component = UnsafeUserSelectMenu<UnsafeModalForm>(timeoutTask)

	builder(component)
	add(component, row)

	return component
}

@UnsafeAPI
public suspend fun <M : UnsafeModalForm>  ComponentContainer.unsafeUserSelectMenu(
	modal: (() -> M)?,
	row: Int? = null,
	builder: suspend UnsafeUserSelectMenu<M>.() -> Unit,
): UnsafeUserSelectMenu<M> {
	val component = UnsafeUserSelectMenu(timeoutTask, modal)

	builder(component)
	add(component, row)

	return component
}

// endregion
