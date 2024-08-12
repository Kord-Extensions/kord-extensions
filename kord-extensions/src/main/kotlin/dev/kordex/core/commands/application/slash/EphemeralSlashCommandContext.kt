/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.commands.application.slash

import dev.kord.core.behavior.interaction.response.EphemeralMessageInteractionResponseBehavior
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kordex.core.commands.Arguments
import dev.kordex.core.components.forms.ModalForm
import dev.kordex.core.types.EphemeralInteractionContext
import dev.kordex.core.utils.MutableStringKeyedMap

/** Ephemeral-only slash command context. **/
public class EphemeralSlashCommandContext<A : Arguments, M : ModalForm>(
	override val event: ChatInputCommandInteractionCreateEvent,
	override val command: SlashCommand<EphemeralSlashCommandContext<A, M>, A, M>,
	override val interactionResponse: EphemeralMessageInteractionResponseBehavior,
	cache: MutableStringKeyedMap<Any>,
) : SlashCommandContext<EphemeralSlashCommandContext<A, M>, A, M>(event, command, cache), EphemeralInteractionContext
