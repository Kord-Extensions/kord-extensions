/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.i18n

import com.ibm.icu.text.MessageFormat
import mu.KLogger
import mu.KotlinLogging
import java.util.*

/**
 * Translation provider backed by Java's [ResourceBundle]s. This makes use of `.properties` files that are standard
 * across the Java ecosystem.
 *
 * Bundles are resolved as follows:
 *
 * * If `bundleName` is `null`, default to `kordex`
 * * Prefix the bundle name with `translations.`
 * * If `bundleName` doesn't contain a `.` character, suffix it with `.strings`
 *
 * With a `bundleName` of `null`, this means the bundle will be named `translations.kordex.strings`, which will resolve
 * to `translations/kordex/strings${_locale ?: ""}.properties` in the resources.
 */
public open class ResourceBundleTranslations(
    defaultLocaleBuilder: () -> Locale,
) : TranslationsProvider(defaultLocaleBuilder) {
    private val logger: KLogger = KotlinLogging.logger(
        "com.kotlindiscord.kord.extensions.i18n.ResourceBundleTranslations"
    )

    private val bundles: MutableMap<Pair<String, Locale>, ResourceBundle> = mutableMapOf()
    private val overrideBundles: MutableMap<Pair<String, Locale>, ResourceBundle> = mutableMapOf()

    public override fun hasKey(key: String, locale: Locale, bundleName: String?): Boolean {
        return try {
            val (bundle, _) = getBundles(locale, bundleName)

            // Overrides aren't for adding keys, so we don't check them
            bundle.keys.toList().contains(key)
        } catch (e: MissingResourceException) {
            logger.trace { "Failed to get bundle $bundleName for locale $locale" }

            false
        }
    }

    /**
     * Loads the [ResourceBundle] called [bundle] for [locale].
     *
     * @see ResourceBundle.getBundle
     */
    protected open fun getResourceBundle(
        bundle: String,
        locale: Locale,
        control: ResourceBundle.Control,
    ): ResourceBundle = ResourceBundle.getBundle(bundle, locale, control)

    /**
     * Retrieves a pair of the [ResourceBundle] and the override resource bundle for [bundleName] in locale.
     */
    @Throws(MissingResourceException::class)
    protected open fun getBundles(locale: Locale, bundleName: String?): Pair<ResourceBundle, ResourceBundle?> {
        val bundle = buildString {
            append("translations." + (bundleName ?: KORDEX_KEY))

            if (this.count { it == '.' } < 2) {
                append(".$DEFAULT_BUNDLE_SUFFIX")
            }
        }

        val bundleKey = bundle to locale

        if (bundles[bundleKey] == null) {
            val localeTag = locale.toLanguageTag()

            logger.trace { "Getting bundle $bundle for locale $locale" }

            val firstBundle = getResourceBundle(bundle, locale, Control)

            bundles[bundleKey] = if (localeTag.count { it in "-_" } != 0) {
                val baseCode = localeTag.split('-', '_').first()
                val secondLocale = Locale(baseCode, baseCode)
                val secondBundle = getResourceBundle(bundle, secondLocale, Control)

                val firstKey = firstBundle.keySet().first()

                if (firstBundle.getStringOrNull(firstKey) != secondBundle.getStringOrNull(firstKey)) {
                    secondBundle
                } else {
                    firstBundle
                }
            } else {
                firstBundle
            }

            try {
                val overrideBundle = bundle + "_override"

                logger.trace { "Getting override bundle $overrideBundle for locale $locale" }

                overrideBundles[bundleKey] = getResourceBundle(overrideBundle, locale, Control)
            } catch (e: MissingResourceException) {
                logger.trace { "No override bundle found." }
            }
        }

        return bundles[bundleKey]!! to overrideBundles[bundleKey]
    }

    @Throws(MissingResourceException::class)
    public override fun get(key: String, locale: Locale, bundleName: String?): String {
        val (bundle, overrideBundle) = getBundles(locale, bundleName)
        val result = overrideBundle?.getStringOrNull(key) ?: bundle.getString(key)

        logger.trace { "Result: $key -> $result" }

        return result
    }

    /**
     * Retrieve a translated string from a [key] in a given [bundleName].
     *
     * The string's parameters are not replaced.
     */
    protected fun getTranslatedString(key: String, locale: Locale, bundleName: String?): String {
        var string = try {
            get(key, locale, bundleName)
        } catch (e: MissingResourceException) {
            key
        }

        return try {
            if (string == key && bundleName != null) {
                // Fall through to the default bundle if the key isn't found
                logger.trace { "'$key' not found in bundle '$bundleName' - falling through to '$KORDEX_KEY'" }

                string = get(key, locale, KORDEX_KEY)
            }
            string
        } catch (e: MissingResourceException) {
            logger.trace {
                if (bundleName == null) {
                    "Unable to find translation for key '$key' in bundle '$KORDEX_KEY'"
                } else {
                    "Unable to find translation for key '$key' in bundles: '$bundleName', '$KORDEX_KEY'"
                }
            }

            key
        }
    }

    override fun translate(key: String, locale: Locale, bundleName: String?, replacements: Array<Any?>): String {
        val string = getTranslatedString(key, locale, bundleName)

        val formatter = MessageFormat(string, locale)

        return formatter.format(replacements)
    }

    override fun translate(key: String, locale: Locale, bundleName: String?, replacements: Map<String, Any?>): String {
        val string = getTranslatedString(key, locale, bundleName)

        val formatter = MessageFormat(string, locale)

        return formatter.format(replacements)
    }

    private fun ResourceBundle.getStringOrNull(key: String): String? {
        return try {
            getString(key)
        } catch (e: MissingResourceException) {
            null
        }
    }

    private object Control : ResourceBundle.Control() {
        override fun getFormats(baseName: String?): MutableList<String> {
            if (baseName == null) {
                throw NullPointerException()
            }

            return FORMAT_PROPERTIES
        }
    }
}
