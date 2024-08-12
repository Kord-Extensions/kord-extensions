/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.modules.dev.unsafe.contexts

import dev.kord.core.behavior.interaction.response.MessageInteractionResponseBehavior
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kordex.core.commands.Arguments
import dev.kordex.core.commands.application.slash.SlashCommandContext
import dev.kordex.core.utils.MutableStringKeyedMap
import dev.kordex.modules.dev.unsafe.annotations.UnsafeAPI
import dev.kordex.modules.dev.unsafe.commands.UnsafeCommandInteractionContext
import dev.kordex.modules.dev.unsafe.commands.slash.UnsafeSlashCommand
import dev.kordex.modules.dev.unsafe.components.forms.UnsafeModalForm

/** Command context for an unsafe slash command. **/
@UnsafeAPI
public class UnsafeCommandSlashCommandContext<A : Arguments, M : UnsafeModalForm>(
	override val event: ChatInputCommandInteractionCreateEvent,
	override val command: UnsafeSlashCommand<A, M>,
	override var interactionResponse: MessageInteractionResponseBehavior?,
	cache: MutableStringKeyedMap<Any>,
) : SlashCommandContext<UnsafeCommandSlashCommandContext<A, M>, A, M>(event, command, cache),
    UnsafeCommandInteractionContext
