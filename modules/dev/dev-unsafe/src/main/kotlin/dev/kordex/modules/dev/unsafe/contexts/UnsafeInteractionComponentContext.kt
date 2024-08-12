/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.modules.dev.unsafe.contexts

import dev.kord.core.behavior.interaction.response.MessageInteractionResponseBehavior
import dev.kord.core.event.interaction.ButtonInteractionCreateEvent
import dev.kordex.core.components.buttons.InteractionButtonContext
import dev.kordex.core.utils.MutableStringKeyedMap
import dev.kordex.modules.dev.unsafe.annotations.UnsafeAPI
import dev.kordex.modules.dev.unsafe.components.buttons.UnsafeButtonInteractionContext
import dev.kordex.modules.dev.unsafe.components.buttons.UnsafeInteractionButton
import dev.kordex.modules.dev.unsafe.components.forms.UnsafeModalForm

@UnsafeAPI
public class UnsafeInteractionComponentContext<M : UnsafeModalForm>(
	override val component: UnsafeInteractionButton<M>,
	override val event: ButtonInteractionCreateEvent,
	override var interactionResponse: MessageInteractionResponseBehavior?,
	cache: MutableStringKeyedMap<Any>,
) : InteractionButtonContext(component, event, cache), UnsafeButtonInteractionContext
