package com.kotlindiscord.kord.extensions.commands.application

import com.kotlindiscord.kord.extensions.commands.Command
import com.kotlindiscord.kord.extensions.commands.CommandContext
import dev.kord.core.behavior.GuildBehavior
import dev.kord.core.behavior.MemberBehavior
import dev.kord.core.behavior.UserBehavior
import dev.kord.core.behavior.channel.ChannelBehavior
import dev.kord.core.entity.interaction.GuildAutoCompleteInteraction
import dev.kord.core.event.interaction.AutoCompleteInteractionCreateEvent

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
