/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.modules.dev.unsafe.contexts

import dev.kord.core.behavior.interaction.response.MessageInteractionResponseBehavior
import dev.kord.core.event.interaction.UserCommandInteractionCreateEvent
import dev.kordex.core.commands.application.user.UserCommand
import dev.kordex.core.commands.application.user.UserCommandContext
import dev.kordex.core.utils.MutableStringKeyedMap
import dev.kordex.modules.dev.unsafe.annotations.UnsafeAPI
import dev.kordex.modules.dev.unsafe.commands.UnsafeCommandInteractionContext
import dev.kordex.modules.dev.unsafe.components.forms.UnsafeModalForm

/** Command context for an unsafe user command. **/
@UnsafeAPI
public class UnsafeCommandUserCommandContext<M : UnsafeModalForm>(
    override val event: UserCommandInteractionCreateEvent,
    override val command: UserCommand<UnsafeCommandUserCommandContext<M>, M>,
    override var interactionResponse: MessageInteractionResponseBehavior?,
    cache: MutableStringKeyedMap<Any>,
) : UserCommandContext<UnsafeCommandUserCommandContext<M>, M>(event, command, cache), UnsafeCommandInteractionContext
