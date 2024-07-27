/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.kordex.core.commands.application.user

import dev.kord.core.behavior.interaction.response.EphemeralMessageInteractionResponseBehavior
import dev.kord.core.event.interaction.UserCommandInteractionCreateEvent
import dev.kordex.core.components.forms.ModalForm
import dev.kordex.core.types.EphemeralInteractionContext
import dev.kordex.core.utils.MutableStringKeyedMap

/** Ephemeral-only user command context. **/
public class EphemeralUserCommandContext<M : ModalForm>(
	override val event: UserCommandInteractionCreateEvent,
	override val command: UserCommand<EphemeralUserCommandContext<M>, M>,
	override val interactionResponse: EphemeralMessageInteractionResponseBehavior,
	cache: MutableStringKeyedMap<Any>,
) : UserCommandContext<EphemeralUserCommandContext<M>, M>(event, command, cache), EphemeralInteractionContext
