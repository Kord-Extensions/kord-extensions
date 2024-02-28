/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.commands.application.slash

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.ApplicationCommandContext
import com.kotlindiscord.kord.extensions.components.forms.ModalForm
import com.kotlindiscord.kord.extensions.utils.MutableStringKeyedMap
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent

/**
 * Slash command context, containing everything you need for your slash command's execution.
 *
 * @param event Event that triggered this slash command invocation.
 */
public open class SlashCommandContext<C : SlashCommandContext<C, A, M>, A : Arguments, M : ModalForm>(
	public open val event: ChatInputCommandInteractionCreateEvent,
	public override val command: SlashCommand<C, A, M>,
	cache: MutableStringKeyedMap<Any>,
) : ApplicationCommandContext(event, command, cache) {
	/** Object representing this slash command's arguments, if any. **/
	public open lateinit var arguments: A

	/** @suppress Internal function for copying args object in later. **/
	public fun populateArgs(args: A) {
		arguments = args
	}
}
