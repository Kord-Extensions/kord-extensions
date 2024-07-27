/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.kordex.core.commands.application

import dev.kord.core.behavior.GuildBehavior
import dev.kord.core.behavior.MemberBehavior
import dev.kord.core.behavior.UserBehavior
import dev.kord.core.behavior.channel.ChannelBehavior
import dev.kord.core.entity.interaction.GuildAutoCompleteInteraction
import dev.kord.core.event.interaction.AutoCompleteInteractionCreateEvent
import dev.kordex.core.commands.Command
import dev.kordex.core.commands.CommandContext

public class DummyAutocompleteCommandContext(
	command: Command,
	private val event: AutoCompleteInteractionCreateEvent,
	commandName: String,
) : CommandContext(command, event, commandName, mutableMapOf()) {
	override suspend fun populate() {
		error("This should never be called.")
	}

	override suspend fun getChannel(): ChannelBehavior =
		event.interaction.channel

	override suspend fun getGuild(): GuildBehavior? =
		(event.interaction as? GuildAutoCompleteInteraction)?.guild

	override suspend fun getMember(): MemberBehavior? =
		(event.interaction as? GuildAutoCompleteInteraction)?.user?.asMemberOrNull()

	override suspend fun getUser(): UserBehavior =
		event.interaction.user
}
