/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.commands.application

import dev.kord.common.asJavaLocale
import dev.kord.common.entity.ApplicationCommandType
import dev.kord.common.entity.Permission
import dev.kord.common.entity.Permissions
import dev.kord.common.entity.Snowflake
import dev.kord.core.event.interaction.InteractionCreateEvent
import dev.kordex.core.DiscordRelayedException
import dev.kordex.core.ExtensibleBot
import dev.kordex.core.checks.hasPermission
import dev.kordex.core.checks.types.CheckContextWithCache
import dev.kordex.core.checks.types.CheckWithCache
import dev.kordex.core.commands.Command
import dev.kordex.core.commands.application.slash.SlashCommand
import dev.kordex.core.extensions.Extension
import dev.kordex.core.i18n.types.Key
import dev.kordex.core.koin.KordExKoinComponent
import dev.kordex.core.utils.MutableStringKeyedMap
import dev.kordex.core.utils.getLocale
import org.koin.core.component.inject
import dev.kord.common.Locale as KLocale

/**
 * Abstract class representing an application command - extend this for actual implementations.
 *
 * @param extension Extension this application command belongs to.
 */
public abstract class ApplicationCommand<E : InteractionCreateEvent>(
	extension: Extension,
) : Command(extension), KordExKoinComponent {
	/** Translations provider, for retrieving translations. **/
	protected val bot: ExtensibleBot by inject()

	/** Quick access to the command registry. **/
	public val registry: ApplicationCommandRegistry by inject()

	/** Discord-side command type, for matching up. **/
	public abstract val type: ApplicationCommandType

	/** @suppress **/
	public open val checkList: MutableList<CheckWithCache<E>> = mutableListOf()

	/** @suppress **/
	public open var guildId: Snowflake? = settings.applicationCommandsBuilder.defaultGuild

	/**
	 * Whether to allow everyone to use this command by default.
	 *
	 * This will be set to `false` automatically by the `allowX` functions, to ensure that they're applied by Discord.
	 */
	public open var allowByDefault: Boolean
		get() = defaultMemberPermissions == null
		set(value) {
			defaultMemberPermissions = if (value) {
				null
			} else {
				Permissions()
			}
		}

	/**
	 * Default set of [Permissions] required to use the command on a guild.
	 *
	 * **Not enforced, read [requirePermission] for more information**
	 */
	public open var defaultMemberPermissions: Permissions? = null

	/**
	 * Enables or disables the command in DMs.
	 *
	 * **Calling [guild] or setting [guildId] will disable this automatically**
	 */
	public open var allowInDms: Boolean = extension.allowApplicationCommandInDMs
		get() {
			if (guildId != null) {
				return false
			}

			return field
		}

	/** Permissions required to be able to run this command. **/
	public override val requiredPerms: MutableSet<Permission> = mutableSetOf()

	/**
	 * A [Localized] version of [name]. Lower-cased if this is a slash command.
	 */
	public val localizedName: Localized<String> by lazy {
		localize(
			name,
			this is SlashCommand<*, *, *>
		)
	}

	/**
	 * This will register a requirement for [permissions] with Discord.
	 *
	 * **These permissions won't get enforced, as Discords UI allows server owners to change them, if you want to
	 * enforce them please also call [hasPermission]**
	 */
	public fun requirePermission(vararg permissions: Permission) {
		val newPermissions = (defaultMemberPermissions ?: Permissions()) + Permissions(*permissions)
		defaultMemberPermissions = newPermissions
	}

	/**
	 * Localizes a property by its [key] for this command.
	 *
	 * @param lowerCase Provide `true` to lower-case all the translations. Discord requires this for some fields.
	 */
	public fun localize(
		key: Key,
		lowerCase: Boolean = false,
	): Localized<String> {
		val bundledKey = key.withBundle(resolvedBundle)

		var default = bundledKey
			.withLocale(translationsProvider.defaultLocale)
			.translate()

		if (lowerCase) {
			default = default.lowercase(translationsProvider.defaultLocale)
		}

		val translations = bot.settings.i18nBuilder.applicationCommandLocales
			.associateWith { locale ->
				val result = bundledKey
					.withLocale(locale.asJavaLocale())
					.translate()

				if (lowerCase) {
					result.lowercase(locale.asJavaLocale())
				} else {
					result
				}
			}.filter { it.value != default }

		return Localized(default, translations.toMutableMap())
	}

	/** Specify a specific guild for this application command to be locked to. **/
	public open fun guild(guild: Snowflake?) {
		this.guildId = guild
	}

	/**
	 * Define a check which must pass for the command to be executed.
	 *
	 * A command may have multiple checks - all checks must pass for the command to be executed.
	 * Checks will be run in the order that they're defined.
	 *
	 * This function can be used DSL-style with a given body, or it can be passed one or more
	 * predefined functions. See the samples for more information.
	 *
	 * @param checks Checks to apply to this command.
	 */
	public open fun check(vararg checks: CheckWithCache<E>) {
		checkList.addAll(checks)
	}

	/**
	 * Overloaded check function to allow for DSL syntax.
	 *
	 * @param check Check to apply to this command.
	 */
	public open fun check(check: CheckWithCache<E>) {
		checkList.add(check)
	}

	/** Called in order to execute the command. **/
	public open suspend fun doCall(event: E): Unit = withLock {
		val cache: MutableStringKeyedMap<Any> = mutableMapOf()

		call(event, cache)
	}

	/** Runs standard checks that can be handled in a generic way, without worrying about subclass-specific checks. **/
	@Throws(DiscordRelayedException::class)
	public open suspend fun runStandardChecks(event: E, cache: MutableStringKeyedMap<Any>): Boolean {
		val locale = event.getLocale()

		checkList.forEach { check ->
			val context = CheckContextWithCache(event, locale, cache)

			check(context)

			if (!context.passed) {
				context.throwIfFailedWithMessage()

				return false
			}
		}

		return true
	}

	/** Override this in order to implement any subclass-specific checks. **/
	@Throws(DiscordRelayedException::class)
	public open suspend fun runChecks(event: E, cache: MutableStringKeyedMap<Any>): Boolean =
		runStandardChecks(event, cache)

	/** Override this to implement the calling logic for your subclass. **/
	public abstract suspend fun call(event: E, cache: MutableStringKeyedMap<Any>)
}

/**
 * Representation of a localized object.
 *
 * @property default the default translations
 * @property translations a map containing all localizations
 * @param T the type of the object
 */
public data class Localized<T>(val default: T, val translations: MutableMap<KLocale, String>)
