/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.kordex.core.commands.application.message

import dev.kord.core.behavior.interaction.response.EphemeralMessageInteractionResponseBehavior
import dev.kord.core.event.interaction.MessageCommandInteractionCreateEvent
import dev.kordex.core.components.forms.ModalForm
import dev.kordex.core.types.EphemeralInteractionContext
import dev.kordex.core.utils.MutableStringKeyedMap

/** Ephemeral-only message command context. **/
public class EphemeralMessageCommandContext<M : ModalForm>(
	override val event: MessageCommandInteractionCreateEvent,
	override val command: MessageCommand<EphemeralMessageCommandContext<M>, M>,
	override val interactionResponse: EphemeralMessageInteractionResponseBehavior,
	cache: MutableStringKeyedMap<Any>,
) : MessageCommandContext<EphemeralMessageCommandContext<M>, M>(event, command, cache), EphemeralInteractionContext
