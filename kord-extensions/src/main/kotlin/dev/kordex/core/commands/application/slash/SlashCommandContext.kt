/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.kordex.core.commands.application.slash

import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kordex.core.commands.Arguments
import dev.kordex.core.components.forms.ModalForm
import dev.kordex.core.utils.MutableStringKeyedMap

/**
 * Slash command context, containing everything you need for your slash command's execution.
 *
 * @param event Event that triggered this slash command invocation.
 */
public open class SlashCommandContext<C : SlashCommandContext<C, A, M>, A : Arguments, M : ModalForm>(
	public open val event: ChatInputCommandInteractionCreateEvent,
	public override val command: SlashCommand<C, A, M>,
	cache: MutableStringKeyedMap<Any>,
) : dev.kordex.core.commands.application.ApplicationCommandContext(event, command, cache) {
	/** Object representing this slash command's arguments, if any. **/
	public open lateinit var arguments: A

	/** @suppress Internal function for copying args object in later. **/
	public fun populateArgs(args: A) {
		arguments = args
	}
}
