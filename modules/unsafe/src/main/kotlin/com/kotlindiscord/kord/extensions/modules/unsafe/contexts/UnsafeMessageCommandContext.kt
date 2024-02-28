/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.modules.unsafe.contexts

import com.kotlindiscord.kord.extensions.commands.application.message.MessageCommand
import com.kotlindiscord.kord.extensions.commands.application.message.MessageCommandContext
import com.kotlindiscord.kord.extensions.components.forms.ModalForm
import com.kotlindiscord.kord.extensions.modules.unsafe.annotations.UnsafeAPI
import com.kotlindiscord.kord.extensions.modules.unsafe.types.UnsafeInteractionContext
import com.kotlindiscord.kord.extensions.utils.MutableStringKeyedMap
import dev.kord.core.behavior.interaction.response.MessageInteractionResponseBehavior
import dev.kord.core.event.interaction.MessageCommandInteractionCreateEvent

/** Command context for an unsafe message command. **/
@UnsafeAPI
public class UnsafeMessageCommandContext<M : ModalForm>(
	override val event: MessageCommandInteractionCreateEvent,
	override val command: MessageCommand<UnsafeMessageCommandContext<M>, M>,
	override var interactionResponse: MessageInteractionResponseBehavior?,
	cache: MutableStringKeyedMap<Any>,
) : MessageCommandContext<UnsafeMessageCommandContext<M>, M>(event, command, cache), UnsafeInteractionContext
