/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.commands.application.message

import dev.kord.core.entity.Message
import dev.kord.core.event.interaction.MessageCommandInteractionCreateEvent
import dev.kordex.core.components.forms.ModalForm
import dev.kordex.core.utils.MutableStringKeyedMap

/**
 *  Message command context, containing everything you need for your message command's execution.
 *
 *  @param event Event that triggered this message command.
 *  @param command Message command instance.
 */
public abstract class MessageCommandContext<C : MessageCommandContext<C, M>, M : ModalForm>(
	public open val event: MessageCommandInteractionCreateEvent,
	public override val command: MessageCommand<C, M>,
	cache: MutableStringKeyedMap<Any>,
) : dev.kordex.core.commands.application.ApplicationCommandContext(event, command, cache) {
	/** Messages that this message command is being executed against. **/
	public val targetMessages: Collection<Message> by lazy { event.interaction.messages.values }
}
