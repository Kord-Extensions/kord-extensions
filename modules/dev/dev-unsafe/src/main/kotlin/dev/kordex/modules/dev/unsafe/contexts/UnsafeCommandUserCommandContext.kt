/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.kordex.modules.dev.unsafe.contexts

import dev.kord.core.behavior.interaction.response.MessageInteractionResponseBehavior
import dev.kord.core.event.interaction.UserCommandInteractionCreateEvent
import dev.kordex.core.commands.application.user.UserCommand
import dev.kordex.core.commands.application.user.UserCommandContext
import dev.kordex.core.components.forms.ModalForm
import dev.kordex.core.utils.MutableStringKeyedMap
import dev.kordex.modules.dev.unsafe.annotations.UnsafeAPI
import dev.kordex.modules.dev.unsafe.types.UnsafeCommandInteractionContext

/** Command context for an unsafe user command. **/
@UnsafeAPI
public class UnsafeCommandUserCommandContext<M : ModalForm>(
    override val event: UserCommandInteractionCreateEvent,
    override val command: UserCommand<UnsafeCommandUserCommandContext<M>, M>,
    override var interactionResponse: MessageInteractionResponseBehavior?,
    cache: MutableStringKeyedMap<Any>,
) : UserCommandContext<UnsafeCommandUserCommandContext<M>, M>(event, command, cache), UnsafeCommandInteractionContext
