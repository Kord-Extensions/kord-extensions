/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.modules.dev.unsafe.components.forms

import dev.kord.core.entity.interaction.ModalSubmitInteraction
import dev.kord.rest.builder.message.create.InteractionResponseCreateBuilder

public typealias InitialEphemeralModalResponseBuilder =
	(suspend InteractionResponseCreateBuilder.(ModalSubmitInteraction) -> Unit)?

public typealias InitialPublicModalResponseBuilder =
	(suspend InteractionResponseCreateBuilder.(ModalSubmitInteraction) -> Unit)?
