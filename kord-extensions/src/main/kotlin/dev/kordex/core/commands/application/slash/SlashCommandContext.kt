/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.commands.application.slash

import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kordex.core.commands.Arguments
import dev.kordex.core.commands.application.ApplicationCommandContext
import dev.kordex.core.components.forms.ModalForm
import dev.kordex.core.utils.MutableStringKeyedMap

/**
 * Slash command context, containing everything you need for your slash command's execution.
 *
 * @param event Event that triggered this slash command invocation.
 */
public open class SlashCommandContext<C : SlashCommandContext<C, A, M>, A : Arguments, M : ModalForm>(
	public open val event: ChatInputCommandInteractionCreateEvent,
	public override val command: SlashCommand<C, A, M>,
	cache: MutableStringKeyedMap<Any>,
) : ApplicationCommandContext(event, command, cache) {
	/** Object representing this slash command's arguments, if any. **/
	public open lateinit var arguments: A

	/** @suppress Internal function for copying args object in later. **/
	public fun populateArgs(args: A) {
		arguments = args
	}
}
