/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.modules.dev.unsafe.components.forms

import dev.kord.common.annotation.KordUnsafe
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.behavior.interaction.response.MessageInteractionResponseBehavior
import dev.kord.core.entity.interaction.ModalSubmitInteraction
import dev.kordex.core.annotations.InternalAPI
import dev.kordex.core.components.forms.ModalForm

public abstract class UnsafeModalForm : ModalForm() {
	public lateinit var initialResponse: InitialUnsafeModalResponse

	@OptIn(KordUnsafe::class)
	@InternalAPI
	public suspend fun respond(interaction: ModalSubmitInteraction?): MessageInteractionResponseBehavior? =
		when (val r = initialResponse) {
			InitialUnsafeModalResponse.EphemeralAck -> interaction?.deferEphemeralResponseUnsafe()
			InitialUnsafeModalResponse.PublicAck -> interaction?.deferPublicResponseUnsafe()

			is InitialUnsafeModalResponse.EphemeralResponse -> interaction?.respondEphemeral {
				r.builder?.invoke(this, interaction)
			}

			is InitialUnsafeModalResponse.PublicResponse -> interaction?.respondPublic {
				r.builder?.invoke(this, interaction)
			}
		}
}
