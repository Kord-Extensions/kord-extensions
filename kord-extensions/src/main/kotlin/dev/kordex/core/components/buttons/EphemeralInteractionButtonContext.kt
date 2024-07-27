/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.kordex.core.components.buttons

import dev.kord.core.behavior.interaction.response.EphemeralMessageInteractionResponseBehavior
import dev.kord.core.event.interaction.ButtonInteractionCreateEvent
import dev.kordex.core.components.forms.ModalForm
import dev.kordex.core.types.EphemeralInteractionContext
import dev.kordex.core.utils.MutableStringKeyedMap

/** Class representing the execution context for an ephemeral-only button. **/
public class EphemeralInteractionButtonContext<M : ModalForm>(
	override val component: EphemeralInteractionButton<M>,
	override val event: ButtonInteractionCreateEvent,
	override val interactionResponse: EphemeralMessageInteractionResponseBehavior,
	cache: MutableStringKeyedMap<Any>,
) : InteractionButtonContext(component, event, cache), EphemeralInteractionContext
