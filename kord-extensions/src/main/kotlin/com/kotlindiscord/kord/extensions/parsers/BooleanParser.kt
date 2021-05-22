package com.kotlindiscord.kord.extensions.parsers

import com.kotlindiscord.kord.extensions.builders.ExtensibleBotBuilder
import com.kotlindiscord.kord.extensions.i18n.TranslationsProvider
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.*

/**
 * Object in charge of parsing strings into [Boolean]s using terms defined in the given locale's translations, falling
 * back to the bot's default locale if the input doesn't match.
 *
 * The relevant translations keys are:
 *
 * * `utils.string.false` for `false` values
 * * `utils.string.true` for `true` values
 *
 * Translations may be split using commas, in which case any of the given values will be suitable.
 */
public object BooleanParser : KoinComponent {
    private val translations: TranslationsProvider by inject()
    private val settings: ExtensibleBotBuilder by inject()

    private val valueCache: MutableMap<Locale, Pair<List<String>, List<String>>> = mutableMapOf()

    /**
     * Parse the given string into a [Boolean] based on the translations for the given locale. Falls back to the bot's
     * default locale as required.
     */
    public fun parse(input: String, locale: Locale): Boolean? {
        if (valueCache[locale] == null) {
            val trueValues = translations.translate("utils.string.true", locale)
                .split(',')
                .map { it.trim() }

            val falseValues = translations.translate("utils.string.false", locale)
                .split(',')
                .map { it.trim() }

            valueCache[locale] = trueValues to falseValues
        }

        val (trueValues, falseValues) = valueCache[locale]!!
        val lowerInput = input.toLowerCase()

        val result = when {
            trueValues.contains(lowerInput) -> true
            falseValues.contains(lowerInput) -> false

            else -> null
        }

        if (result == null && locale != settings.i18nBuilder.defaultLocale) {
            // Try it again in the default locale as a fallback

            return parse(input, settings.i18nBuilder.defaultLocale)
        }

        return result
    }
}
