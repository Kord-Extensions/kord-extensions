/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.commands.application.message

import com.kotlindiscord.kord.extensions.components.forms.ModalForm
import com.kotlindiscord.kord.extensions.types.EphemeralInteractionContext
import com.kotlindiscord.kord.extensions.utils.MutableStringKeyedMap
import dev.kord.core.behavior.interaction.response.EphemeralMessageInteractionResponseBehavior
import dev.kord.core.event.interaction.MessageCommandInteractionCreateEvent

/** Ephemeral-only message command context. **/
public class EphemeralMessageCommandContext<M : ModalForm>(
	override val event: MessageCommandInteractionCreateEvent,
	override val command: MessageCommand<EphemeralMessageCommandContext<M>, M>,
	override val interactionResponse: EphemeralMessageInteractionResponseBehavior,
	cache: MutableStringKeyedMap<Any>,
) : MessageCommandContext<EphemeralMessageCommandContext<M>, M>(event, command, cache), EphemeralInteractionContext
