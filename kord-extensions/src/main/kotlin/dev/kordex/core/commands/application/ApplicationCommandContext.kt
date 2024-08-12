/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

@file:OptIn(KordUnsafe::class, KordExperimental::class)

package dev.kordex.core.commands.application

import dev.kord.common.annotation.KordExperimental
import dev.kord.common.annotation.KordUnsafe
import dev.kord.common.entity.Permissions
import dev.kord.core.behavior.GuildBehavior
import dev.kord.core.behavior.MemberBehavior
import dev.kord.core.behavior.UserBehavior
import dev.kord.core.behavior.channel.MessageChannelBehavior
import dev.kord.core.entity.interaction.GuildApplicationCommandInteraction
import dev.kord.core.event.interaction.ApplicationCommandInteractionCreateEvent
import dev.kordex.core.builders.ExtensibleBotBuilder
import dev.kordex.core.commands.CommandContext
import dev.kordex.core.utils.MutableStringKeyedMap
import org.koin.core.component.inject

/**
 * Base class representing the shared functionality for an application command's context.
 *
 * @param genericEvent Generic event object to populate data from.
 * @param genericCommand Generic command object that this context belongs to.
 */
public abstract class ApplicationCommandContext(
	public val genericEvent: ApplicationCommandInteractionCreateEvent,
	public val genericCommand: ApplicationCommand<*>,
	cache: MutableStringKeyedMap<Any>,
) : CommandContext(genericCommand, genericEvent, genericCommand.name, cache) {
	/** Current bot setting object. **/
	public val botSettings: ExtensibleBotBuilder by inject()

	/** Channel this command was executed within. **/
	public open lateinit var channel: MessageChannelBehavior

	/** Guild this command was executed within, if any. **/
	public open var guild: GuildBehavior? = null

	/** Member that executed this command, if on a guild. **/
	public open var member: MemberBehavior? = null

	/** User that executed this command. **/
	public open lateinit var user: UserBehavior

	/**
	 * The permissions applicable to your bot in this execution context (guild, roles, channels), or null if
	 * this command wasn't executed on a guild.
	 */
	public val appPermissions: Permissions? = (genericEvent.interaction as? GuildApplicationCommandInteraction)
		?.appPermissions

	/** Called before processing, used to populate any extra variables from event data. **/
	public override suspend fun populate() {
		// NOTE: This must always be alphabetical, some latter calls rely on earlier ones

		channel = getChannel()
		guild = getGuild()
		member = getMember()
		user = getUser()
	}

	/** Extract channel information from event data, if that context is available. **/
	public override suspend fun getChannel(): MessageChannelBehavior =
		genericEvent.interaction.channel

	/** Extract guild information from event data, if that context is available. **/
	public override suspend fun getGuild(): GuildBehavior? =
		genericEvent.interaction.data.guildId.value?.let { genericEvent.kord.unsafe.guild(it) }

	/** Extract member information from event data, if that context is available. **/
	public override suspend fun getMember(): MemberBehavior? =
		(genericEvent.interaction as? GuildApplicationCommandInteraction)?.user

	/** Extract user information from event data, if that context is available. **/
	public override suspend fun getUser(): UserBehavior =
		genericEvent.interaction.user
}
