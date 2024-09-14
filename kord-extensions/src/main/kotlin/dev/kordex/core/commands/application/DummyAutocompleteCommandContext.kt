/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
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
import dev.kordex.core.i18n.types.Key

public class DummyAutocompleteCommandContext(
	command: Command,
	private val event: AutoCompleteInteractionCreateEvent,
	commandName: Key,
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
