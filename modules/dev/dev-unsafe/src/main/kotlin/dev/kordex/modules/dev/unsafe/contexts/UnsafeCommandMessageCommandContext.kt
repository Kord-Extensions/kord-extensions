/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.modules.dev.unsafe.contexts

import dev.kord.core.behavior.interaction.response.MessageInteractionResponseBehavior
import dev.kord.core.event.interaction.MessageCommandInteractionCreateEvent
import dev.kordex.core.commands.application.message.MessageCommand
import dev.kordex.core.commands.application.message.MessageCommandContext
import dev.kordex.core.utils.MutableStringKeyedMap
import dev.kordex.modules.dev.unsafe.annotations.UnsafeAPI
import dev.kordex.modules.dev.unsafe.commands.UnsafeCommandInteractionContext
import dev.kordex.modules.dev.unsafe.components.forms.UnsafeModalForm

/** Command context for an unsafe message command. **/
@UnsafeAPI
public class UnsafeCommandMessageCommandContext<M : UnsafeModalForm>(
	override val event: MessageCommandInteractionCreateEvent,
	override val command: MessageCommand<UnsafeCommandMessageCommandContext<M>, M>,
	override var interactionResponse: MessageInteractionResponseBehavior?,
	cache: MutableStringKeyedMap<Any>,
) : MessageCommandContext<UnsafeCommandMessageCommandContext<M>, M>(event, command, cache),
    UnsafeCommandInteractionContext
