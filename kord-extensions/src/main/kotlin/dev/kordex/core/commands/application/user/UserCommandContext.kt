/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.commands.application.user

import dev.kord.core.entity.User
import dev.kord.core.event.interaction.UserCommandInteractionCreateEvent
import dev.kordex.core.components.forms.ModalForm
import dev.kordex.core.utils.MutableStringKeyedMap

/**
 *  User command context, containing everything you need for your user command's execution.
 *
 *  @param event Event that triggered this message command.
 *  @param command Message command instance.
 */
public abstract class UserCommandContext<C : UserCommandContext<C, M>, M : ModalForm>(
	public open val event: UserCommandInteractionCreateEvent,
	public override val command: UserCommand<C, M>,
	cache: MutableStringKeyedMap<Any>,
) : dev.kordex.core.commands.application.ApplicationCommandContext(event, command, cache) {
	/** Messages that this message command is being executed against. **/
	public val targetUsers: Collection<User> by lazy { event.interaction.users.values }
}
