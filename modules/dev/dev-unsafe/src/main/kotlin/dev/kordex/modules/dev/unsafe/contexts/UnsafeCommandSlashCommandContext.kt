/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.kordex.modules.dev.unsafe.contexts

import dev.kord.core.behavior.interaction.response.MessageInteractionResponseBehavior
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kordex.core.commands.Arguments
import dev.kordex.core.commands.application.slash.SlashCommandContext
import dev.kordex.core.utils.MutableStringKeyedMap
import dev.kordex.modules.dev.unsafe.annotations.UnsafeAPI
import dev.kordex.modules.dev.unsafe.commands.UnsafeCommandInteractionContext
import dev.kordex.modules.dev.unsafe.commands.slash.UnsafeSlashCommand
import dev.kordex.modules.dev.unsafe.components.forms.UnsafeModalForm

/** Command context for an unsafe slash command. **/
@UnsafeAPI
public class UnsafeCommandSlashCommandContext<A : Arguments, M : UnsafeModalForm>(
	override val event: ChatInputCommandInteractionCreateEvent,
	override val command: UnsafeSlashCommand<A, M>,
	override var interactionResponse: MessageInteractionResponseBehavior?,
	cache: MutableStringKeyedMap<Any>,
) : SlashCommandContext<UnsafeCommandSlashCommandContext<A, M>, A, M>(event, command, cache),
    UnsafeCommandInteractionContext
