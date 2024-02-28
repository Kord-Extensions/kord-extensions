/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.components.buttons

import com.kotlindiscord.kord.extensions.components.forms.ModalForm
import com.kotlindiscord.kord.extensions.types.EphemeralInteractionContext
import com.kotlindiscord.kord.extensions.utils.MutableStringKeyedMap
import dev.kord.core.behavior.interaction.response.EphemeralMessageInteractionResponseBehavior
import dev.kord.core.event.interaction.ButtonInteractionCreateEvent

/** Class representing the execution context for an ephemeral-only button. **/
public class EphemeralInteractionButtonContext<M : ModalForm>(
	override val component: EphemeralInteractionButton<M>,
	override val event: ButtonInteractionCreateEvent,
	override val interactionResponse: EphemeralMessageInteractionResponseBehavior,
	cache: MutableStringKeyedMap<Any>,
) : InteractionButtonContext(component, event, cache), EphemeralInteractionContext
