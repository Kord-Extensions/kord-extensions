/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

@file:Suppress("UnnecessaryAbstractClass")  // No idea why we're getting this

package dev.kordex.core.commands

import dev.kord.common.entity.Permission
import dev.kord.core.Kord
import dev.kord.core.behavior.channel.asChannelOfOrNull
import dev.kord.core.entity.channel.GuildChannel
import dev.kordex.core.DiscordRelayedException
import dev.kordex.core.InvalidCommandException
import dev.kordex.core.annotations.ExtensionDSL
import dev.kordex.core.builders.ExtensibleBotBuilder
import dev.kordex.core.commands.events.CommandEvent
import dev.kordex.core.extensions.Extension
import dev.kordex.core.i18n.TranslationsProvider
import dev.kordex.core.i18n.generated.CoreTranslations
import dev.kordex.core.i18n.types.Key
import dev.kordex.core.koin.KordExKoinComponent
import dev.kordex.core.sentry.SentryAdapter
import dev.kordex.core.types.Lockable
import dev.kordex.core.utils.permissionsForMember
import dev.kordex.core.utils.translate
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import org.koin.core.component.inject
import java.util.*

/**
 * Abstract base class representing the few things that command objects can have in common.
 *
 * This should be used as a base class only for command types that aren't related to the other command types.
 *
 * @property extension The extension object this command belongs to.
 */
@ExtensionDSL
public abstract class Command(public val extension: Extension) : Lockable, KordExKoinComponent {
	/**
	 * The name of this command, for invocation and help commands.
	 */
	public open lateinit var name: Key

	/** Set this to `true` to lock command execution with a Mutex. **/
	public override var locking: Boolean = false

	override var mutex: Mutex? = null

	/** Translations provider, for retrieving translations. **/
	public val translationsProvider: TranslationsProvider by inject()

	/** Bot settings object. **/
	public val settings: ExtensibleBotBuilder by inject()

	/** Sentry adapter, for easy access to Sentry functions. **/
	public val sentry: SentryAdapter by inject()

	/** Kord instance, backing the ExtensibleBot. **/
	public val kord: Kord by inject()

	/** Permissions required to be able to run this command. **/
	public open val requiredPerms: MutableSet<Permission> = mutableSetOf()

	/** Translation cache, so we don't have to look up translations every time. **/
	public open val nameTranslationCache: MutableMap<Locale, String> = mutableMapOf()

	/**
	 * An internal function used to ensure that all of a command's required arguments are present and correct.
	 *
	 * @throws InvalidCommandException Thrown when a required argument hasn't been set or is invalid.
	 */
	@Throws(InvalidCommandException::class)
	public open fun validate() {
		if (!::name.isInitialized || name.key.isEmpty()) {
			throw InvalidCommandException(null, "No command name given.")
		}

		if (locking && mutex == null) {
			mutex = Mutex()
		}
	}

	/** Quick shortcut for emitting a command event without blocking. **/
	public open suspend fun emitEventAsync(event: CommandEvent<*, *>): Job =
		kord.launch {
			extension.bot.send(event)
		}

	/** Checks whether the bot has the specified required permissions, throwing if it doesn't. **/
	@Throws(DiscordRelayedException::class)
	public open suspend fun checkBotPerms(context: CommandContext) {
		if (requiredPerms.isEmpty()) {
			return  // Nothing to check, don't try to hit the cache
		}

		if (context.getGuild() != null) {
			val perms = context
				.getChannel()
				.asChannelOfOrNull<GuildChannel>()
				?.permissionsForMember(kord.selfId)
				?: return // Nothing to check if we can't get the channel.

			val missingPerms = requiredPerms.filter { !perms.contains(it) }

			if (missingPerms.isNotEmpty()) {
				throw DiscordRelayedException(
					CoreTranslations.Commands.Error.missingBotPermissions
						.withLocale(context.getLocale())
						.withOrdinalPlaceholders(
							missingPerms
								.map { it.translate(context.getLocale()) }
								.joinToString()
						)
				)
			}
		}
	}

	/** If your bot requires permissions to be able to execute the command, add them using this function. **/
	public fun requireBotPermissions(vararg perms: Permission) {
		perms.forEach(requiredPerms::add)
	}
}
