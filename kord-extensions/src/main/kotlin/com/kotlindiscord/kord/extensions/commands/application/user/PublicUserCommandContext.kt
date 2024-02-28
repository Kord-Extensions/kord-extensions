/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.commands.application.user

import com.kotlindiscord.kord.extensions.components.forms.ModalForm
import com.kotlindiscord.kord.extensions.types.PublicInteractionContext
import com.kotlindiscord.kord.extensions.utils.MutableStringKeyedMap
import dev.kord.core.behavior.interaction.response.PublicMessageInteractionResponseBehavior
import dev.kord.core.event.interaction.UserCommandInteractionCreateEvent

/** Public-only user command context. **/
public class PublicUserCommandContext<M : ModalForm>(
	override val event: UserCommandInteractionCreateEvent,
	override val command: UserCommand<PublicUserCommandContext<M>, M>,
	override val interactionResponse: PublicMessageInteractionResponseBehavior,
	cache: MutableStringKeyedMap<Any>,
) : UserCommandContext<PublicUserCommandContext<M>, M>(event, command, cache), PublicInteractionContext
