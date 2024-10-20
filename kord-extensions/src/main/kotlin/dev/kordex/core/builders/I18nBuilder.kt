/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.builders

import dev.kord.common.asJavaLocale
import dev.kord.common.kLocale
import dev.kord.core.entity.interaction.Interaction
import dev.kordex.core.annotations.BotBuilderDSL
import dev.kordex.core.i18n.ResourceBundleTranslations
import dev.kordex.core.i18n.SupportedLocales
import dev.kordex.core.i18n.TranslationsProvider
import java.util.Locale
import dev.kord.common.Locale as KLocale

/** Builder used to configure i18n options. **/
@BotBuilderDSL
public class I18nBuilder {

	public var defaultLocale: Locale = SupportedLocales.ENGLISH

	/**
	 * List of [locales][KLocale] which are used for application command names (without [defaultLocale]).
	 */
	public var applicationCommandLocales: MutableList<KLocale> = mutableListOf()

	/**
	 * Callables used to resolve a Locale object for the given guild, channel, and user.
	 *
	 * Resolves to [defaultLocale] by default.
	 */
	public var localeResolvers: MutableList<LocaleResolver> = mutableListOf()

	/** Object responsible for retrieving translations. Users should get this via Koin or other methods. **/
	internal var translationsProvider: TranslationsProvider = ResourceBundleTranslations { defaultLocale }

	/** Call this with a builder (usually the class constructor) to set the translations provider. **/
	public fun translationsProvider(builder: (() -> Locale) -> TranslationsProvider) {
		translationsProvider = builder { defaultLocale }
	}

	/** Register a locale resolver, returning the required [Locale] object or `null`. **/
	public fun localeResolver(body: LocaleResolver) {
		localeResolvers.add(body)
	}

	/**
	 * Registers [locales] as application command languages.
	 *
	 * **Do not register [defaultLocale]!**
	 */
	@JvmName("applicationCommandLocale_v1")
	public fun applicationCommandLocale(
		vararg locales: KLocale,
	) {
		applicationCommandLocales.addAll(locales.toList())
	}

	/**
	 * Registers [locales] as application command languages.
	 *
	 * **Do not register [defaultLocale]!**
	 */
	@JvmName("applicationCommandLocale_v2")
	public fun applicationCommandLocale(
		vararg locales: Locale,
	) {
		applicationCommandLocales.addAll(locales.map { it.kLocale })
	}

	/**
	 * Registers [locales] as application command languages.
	 *
	 * **Do not register [defaultLocale]!**
	 */
	@JvmName("applicationCommandLocale_c1")
	public fun applicationCommandLocale(
		locales: Collection<KLocale>,
	) {
		applicationCommandLocales.addAll(locales)
	}

	/**
	 * Registers [locales] as application command languages.
	 *
	 * **Do not register [defaultLocale]!**
	 */
	@JvmName("applicationCommandLocale_c2")
	public fun applicationCommandLocale(
		locales: Collection<Locale>,
	) {
		applicationCommandLocales.addAll(locales.map { it.kLocale })
	}

	/**
	 * Registers a [LocaleResolver] using [Interaction.locale].
	 */
	public fun interactionUserLocaleResolver(): Unit =
		localeResolver { _, _, _, interaction ->
			interaction?.locale?.asJavaLocale()
		}

	/**
	 * Registers a [LocaleResolver] using [Interaction.guildLocale].
	 */
	public fun interactionGuildLocaleResolver(): Unit =
		localeResolver { _, _, _, interaction ->
			interaction?.guildLocale?.asJavaLocale()
		}
}
