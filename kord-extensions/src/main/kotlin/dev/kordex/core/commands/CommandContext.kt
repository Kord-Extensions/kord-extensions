/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.commands

import dev.kord.core.behavior.GuildBehavior
import dev.kord.core.behavior.MemberBehavior
import dev.kord.core.behavior.UserBehavior
import dev.kord.core.behavior.channel.ChannelBehavior
import dev.kord.core.event.Event
import dev.kordex.core.annotations.ExtensionDSL
import dev.kordex.core.checks.channelFor
import dev.kordex.core.checks.guildFor
import dev.kordex.core.checks.interactionFor
import dev.kordex.core.checks.userFor
import dev.kordex.core.i18n.types.Key
import dev.kordex.core.koin.KordExKoinComponent
import dev.kordex.core.sentry.SentryContext
import dev.kordex.core.types.TranslatableContext
import dev.kordex.core.utils.MutableStringKeyedMap
import java.util.*

/**
 * Light wrapper class representing the context for a command's action.
 *
 * This is what `this` refers to in a command action body. You shouldn't have to
 * instantiate this yourself.
 *
 * @param command Respective command for this context object.
 * @param eventObj Event that triggered this command.
 * @param commandName Command name given by the user to invoke the command - lower-cased.
 * @param cache Data cache map shared with the defined checks.
 */
@ExtensionDSL
public abstract class CommandContext(
	public open val command: Command,
	public open val eventObj: Event,
	public open val commandName: Key,
	public open val cache: MutableStringKeyedMap<Any>,
) : KordExKoinComponent, TranslatableContext {
	/** Current Sentry context, containing breadcrumbs and other goodies. **/
	public val sentry: SentryContext = SentryContext()

	public override var resolvedLocale: Locale? = null

	/** Called before processing, used to populate any extra variables from event data. **/
	public abstract suspend fun populate()

	/** Extract channel information from event data. **/
	public abstract suspend fun getChannel(): ChannelBehavior

	/** Extract guild information from event data, if that context is available. **/
	public abstract suspend fun getGuild(): GuildBehavior?

	/** Extract member information from event data, if that context is available. **/
	public abstract suspend fun getMember(): MemberBehavior?

	/** Extract user information from event data, if that context is available. **/
	public abstract suspend fun getUser(): UserBehavior?

	public override suspend fun getLocale(): Locale {
		var locale: Locale? = resolvedLocale

		if (locale != null) {
			return locale
		}

		val guild = guildFor(eventObj)
		val channel = channelFor(eventObj)
		val user = userFor(eventObj)

		for (resolver in command.extension.bot.settings.i18nBuilder.localeResolvers) {
			val result = resolver(guild, channel, user, interactionFor(eventObj))

			if (result != null) {
				locale = result
				break
			}
		}

		resolvedLocale = locale ?: command.extension.bot.settings.i18nBuilder.defaultLocale

		return resolvedLocale!!
	}
}
