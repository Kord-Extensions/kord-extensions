/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.commands.application.user

import com.kotlindiscord.kord.extensions.components.forms.ModalForm
import com.kotlindiscord.kord.extensions.types.EphemeralInteractionContext
import com.kotlindiscord.kord.extensions.utils.MutableStringKeyedMap
import dev.kord.core.behavior.interaction.response.EphemeralMessageInteractionResponseBehavior
import dev.kord.core.event.interaction.UserCommandInteractionCreateEvent

/** Ephemeral-only user command context. **/
public class EphemeralUserCommandContext<M : ModalForm>(
	override val event: UserCommandInteractionCreateEvent,
	override val command: UserCommand<EphemeralUserCommandContext<M>, M>,
	override val interactionResponse: EphemeralMessageInteractionResponseBehavior,
	cache: MutableStringKeyedMap<Any>,
) : UserCommandContext<EphemeralUserCommandContext<M>, M>(event, command, cache), EphemeralInteractionContext
