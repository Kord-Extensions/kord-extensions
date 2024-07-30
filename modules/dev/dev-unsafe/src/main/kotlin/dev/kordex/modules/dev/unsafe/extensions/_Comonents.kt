/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.kordex.modules.dev.unsafe.extensions

import dev.kordex.core.components.ComponentContainer
import dev.kordex.core.components.forms.ModalForm
import dev.kordex.modules.dev.unsafe.annotations.UnsafeAPI
import dev.kordex.modules.dev.unsafe.components.UnsafeInteractionButton

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
