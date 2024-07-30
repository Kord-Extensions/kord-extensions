/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.kordex.modules.dev.unsafe.contexts

import dev.kord.core.behavior.interaction.response.MessageInteractionResponseBehavior
import dev.kord.core.event.interaction.ButtonInteractionCreateEvent
import dev.kordex.core.components.buttons.InteractionButtonContext
import dev.kordex.core.components.forms.ModalForm
import dev.kordex.core.utils.MutableStringKeyedMap
import dev.kordex.modules.dev.unsafe.annotations.UnsafeAPI
import dev.kordex.modules.dev.unsafe.components.buttons.UnsafeButtonInteractionContext
import dev.kordex.modules.dev.unsafe.components.buttons.UnsafeInteractionButton

@UnsafeAPI
public class UnsafeInteractionComponentContext<M : ModalForm>(
	override val component: UnsafeInteractionButton<M>,
	override val event: ButtonInteractionCreateEvent,
	override var interactionResponse: MessageInteractionResponseBehavior?,
	cache: MutableStringKeyedMap<Any>,
) : InteractionButtonContext(component, event, cache), UnsafeButtonInteractionContext
