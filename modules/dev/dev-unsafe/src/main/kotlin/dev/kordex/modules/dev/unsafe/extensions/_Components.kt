/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.kordex.modules.dev.unsafe.extensions

import dev.kordex.core.components.ComponentContainer
import dev.kordex.core.components.forms.ModalForm
import dev.kordex.modules.dev.unsafe.annotations.UnsafeAPI
import dev.kordex.modules.dev.unsafe.components.buttons.UnsafeInteractionButton
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
	builder: suspend UnsafeInteractionButton<ModalForm>.() -> Unit,
): UnsafeInteractionButton<ModalForm> {
	val component = UnsafeInteractionButton<ModalForm>(timeoutTask)

	builder(component)
	add(component, row)

	return component
}

/** DSL function for creating an unsafe button with modal, and adding it to the current [ComponentContainer]. **/
@UnsafeAPI
public suspend fun <M : ModalForm> ComponentContainer.unsafeButton(
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
	builder: suspend UnsafeChannelSelectMenu<ModalForm>.() -> Unit,
): UnsafeChannelSelectMenu<ModalForm> {
	val component = UnsafeChannelSelectMenu<ModalForm>(timeoutTask)

	builder(component)
	add(component, row)

	return component
}

@UnsafeAPI
public suspend fun <M : ModalForm>  ComponentContainer.unsafeChannelSelectMenu(
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
	builder: suspend UnsafeMentionableSelectMenu<ModalForm>.() -> Unit,
): UnsafeMentionableSelectMenu<ModalForm> {
	val component = UnsafeMentionableSelectMenu<ModalForm>(timeoutTask)

	builder(component)
	add(component, row)

	return component
}

@UnsafeAPI
public suspend fun <M : ModalForm>  ComponentContainer.unsafeMentionableSelectMenu(
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
	builder: suspend UnsafeRoleSelectMenu<ModalForm>.() -> Unit,
): UnsafeRoleSelectMenu<ModalForm> {
	val component = UnsafeRoleSelectMenu<ModalForm>(timeoutTask)

	builder(component)
	add(component, row)

	return component
}

@UnsafeAPI
public suspend fun <M : ModalForm>  ComponentContainer.unsafeRoleSelectMenu(
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
	builder: suspend UnsafeStringSelectMenu<ModalForm>.() -> Unit,
): UnsafeStringSelectMenu<ModalForm> {
	val component = UnsafeStringSelectMenu<ModalForm>(timeoutTask)

	builder(component)
	add(component, row)

	return component
}

@UnsafeAPI
public suspend fun <M : ModalForm>  ComponentContainer.unsafeStringSelectMenu(
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
	builder: suspend UnsafeUserSelectMenu<ModalForm>.() -> Unit,
): UnsafeUserSelectMenu<ModalForm> {
	val component = UnsafeUserSelectMenu<ModalForm>(timeoutTask)

	builder(component)
	add(component, row)

	return component
}

@UnsafeAPI
public suspend fun <M : ModalForm>  ComponentContainer.unsafeUserSelectMenu(
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
