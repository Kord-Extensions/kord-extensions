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
public class ResourceBundleTranslations(
    defaultLocaleBuilder: () -> Locale
) : TranslationsProvider(defaultLocaleBuilder) {
    private val logger: KLogger = KotlinLogging.logger {}
    private val bundles: MutableMap<Pair<String, Locale>, ResourceBundle> = mutableMapOf()

    public override fun hasKey(key: String, locale: Locale, bundleName: String?): Boolean {
        return try {
            val bundleObj = getBundle(locale, bundleName)

            bundleObj.keys.toList().contains(key)
        } catch (e: MissingResourceException) {
            logger.warn(e) { "Failed to get bundle $bundleName for locale $locale" }

            false
        }
    }

    @Throws(MissingResourceException::class)
    private fun getBundle(locale: Locale, bundleName: String?): ResourceBundle {
        var bundle = "translations." + (bundleName ?: KORDEX_KEY)

        if (bundle.count { it == '.' } < 2) {
            bundle += ".$DEFAULT_BUNDLE_SUFFIX"
        }

        val bundleKey = bundle to locale

        logger.debug { "Getting bundle $bundleKey for locale $locale" }
        bundles[bundleKey] = bundles[bundleKey] ?: ResourceBundle.getBundle(bundle, locale, Control)

        return bundles[bundleKey]!!
    }

    @Throws(MissingResourceException::class)
    public override fun get(key: String, locale: Locale, bundleName: String?): String {
        val result = getBundle(locale, bundleName).getString(key)

        logger.debug { "Result: $key -> $result" }

        return result
    }

    override fun translate(key: String, locale: Locale, bundleName: String?, replacements: Array<Any?>): String {
        return try {
            var string = get(key, locale, bundleName)

            if (string == key && bundleName != null) {
                // Fall through to the default bundle if the key isn't found
                logger.debug { "$key not found in bundle $bundleName - falling through to $KORDEX_KEY" }

                string = get(key, locale, KORDEX_KEY)
            }

            val formatter = MessageFormat(string, locale)

            formatter.format(replacements)
        } catch (e: MissingResourceException) {
            logger.debug(e) { "Unable to find translation for key '$key' in bundle '$bundleName'" }

            key
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
