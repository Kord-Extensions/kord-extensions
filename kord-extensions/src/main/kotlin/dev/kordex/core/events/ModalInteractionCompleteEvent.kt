/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.events

import dev.kord.core.entity.interaction.ModalSubmitInteraction

/**
 * Event fired when a modal interaction has completed. Used by commands to figure out when to run their actions.
 *
 * @param id Unique ID for the modal form.
 * @param interaction Interaction object provided by the corresponding event.
 */
public class ModalInteractionCompleteEvent(
	public val id: String,
	public val interaction: ModalSubmitInteraction,
) : KordExEvent
