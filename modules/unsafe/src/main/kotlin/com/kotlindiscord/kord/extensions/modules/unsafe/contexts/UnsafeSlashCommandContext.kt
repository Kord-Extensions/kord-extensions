/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.modules.unsafe.contexts

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.SlashCommandContext
import com.kotlindiscord.kord.extensions.components.forms.ModalForm
import com.kotlindiscord.kord.extensions.modules.unsafe.annotations.UnsafeAPI
import com.kotlindiscord.kord.extensions.modules.unsafe.commands.UnsafeSlashCommand
import com.kotlindiscord.kord.extensions.modules.unsafe.types.UnsafeInteractionContext
import com.kotlindiscord.kord.extensions.utils.MutableStringKeyedMap
import dev.kord.core.behavior.interaction.response.MessageInteractionResponseBehavior
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent

/** Command context for an unsafe slash command. **/
@UnsafeAPI
public class UnsafeSlashCommandContext<A : Arguments, M : ModalForm>(
	override val event: ChatInputCommandInteractionCreateEvent,
	override val command: UnsafeSlashCommand<A, M>,
	override var interactionResponse: MessageInteractionResponseBehavior?,
	cache: MutableStringKeyedMap<Any>,
) : SlashCommandContext<UnsafeSlashCommandContext<A, M>, A, M>(event, command, cache), UnsafeInteractionContext
