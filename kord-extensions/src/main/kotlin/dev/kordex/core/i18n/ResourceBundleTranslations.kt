/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.i18n

import com.ibm.icu.text.MessageFormat
import dev.kordex.core.builders.ExtensibleBotBuilder
import dev.kordex.core.i18n.types.Key
import dev.kordex.core.koin.KordExKoinComponent
import dev.kordex.core.plugins.PluginManager
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import org.koin.core.component.inject
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
	private val logger: KLogger = KotlinLogging.logger { }

	private val pluginManager: PluginManager by inject()

	private val bundles: MutableMap<Pair<String, Locale>, ResourceBundle> = mutableMapOf()
	private val overrideBundles: MutableMap<Pair<String, Locale>, ResourceBundle> = mutableMapOf()

	public override fun hasKey(key: Key): Boolean {
		val (key, bundle, locale) = key

		return try {
			val (bundle, _) = getBundles(bundle?.name, locale)

			// Overrides aren't for adding keys, so we don't check them.
			bundle.keys.toList().contains(key)
		} catch (e: MissingResourceException) {
			logger.trace(e) { "Failed to get $bundle for locale $locale" }

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
	): ResourceBundle {
		val defaultBundlePath = bundle.replace(".", "/") + ".properties"

		val classLoaders: MutableList<Pair<String, ClassLoader>> = mutableListOf(
			"default class-loader" to ResourceBundleTranslations::class.java.classLoader,
			"system class-loader" to ClassLoader.getSystemClassLoader(),
		)

		if (pluginManager.enabled) {
			pluginManager.plugins.forEach { plugin ->
				classLoaders.add(plugin.pluginId to plugin.pluginClassLoader)
			}
		}

		classLoaders.forEach { (plugin, loader) ->
			logger.trace { "Trying to find $bundle with ${locale.toLanguageTag()} in $plugin" }

			try {
				if (loader.getResource(defaultBundlePath) != null) {
					val result = ResourceBundle.getBundle(bundle, locale, loader, control)

					logger.debug { "Found bundle $bundle with ${locale.toLanguageTag()} in $plugin" }

					return result
				}
			} catch (_: MissingResourceException) {
				null  // Do nothing, we expect this to happen.
			}
		}

		logger.debug { "Couldn't find bundle $bundle with ${locale.toLanguageTag()}; falling back to default strategy" }

		return ResourceBundle.getBundle(bundle, locale, control)
	}

	/**
	 * Retrieves a pair of the [ResourceBundle] and the override resource bundle for [bundleName] in locale.
	 */
	@Throws(MissingResourceException::class)
	protected open fun getBundles(bundleName: String?, nullableLocale: Locale?): Pair<ResourceBundle, ResourceBundle?> {
		val locale = nullableLocale
			?: defaultLocale

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
				logger.trace(e) { "No override bundle found." }
			}
		}

		return bundles[bundleKey]!! to overrideBundles[bundleKey]
	}

	@Throws(MissingResourceException::class)
	public override fun get(key: Key): String {
		val (key, bundle, locale) = key

		val (baseBundle, overrideBundle) = getBundles(bundle?.name, locale)
		val result = overrideBundle?.getStringOrNull(key) ?: baseBundle.getString(key)

		logger.trace { "Result: $key -> $result" }

		return result
	}

	/** Retrieve an unformatted, translated string from a translation [key]. **/
	protected fun getTranslatedString(key: Key): String {
		val bundle = key.bundle

		var string: String = try {
			get(key)
		} catch (_: MissingResourceException) {
			key.key
		}

		return try {
			if (string == key.key && bundle != null) {
				// Fall through to the default bundle if the key isn't found
				logger.trace { "$key not found - falling through to $KORDEX_BUNDLE" }

				string = get(key.withBundle(KORDEX_BUNDLE))
			}

			string
		} catch (e: MissingResourceException) {
			logger.trace(e) {
				if (bundle == null) {
					"Unable to find translation for $key"
				} else {
					"Unable to find translation for ${key.withoutBundle()} in $bundle or $KORDEX_BUNDLE"
				}
			}

			key.key
		}
	}

	override fun translate(
		key: Key,
		replacements: Array<Any?>,
	): String {
		val locale = key.locale

		val string = getTranslatedString(key)
		val formatter = MessageFormat(string, locale)

		return formatter.format(replacements)
	}

	override fun translateNamed(
		key: Key,
		replacements: Map<String, Any?>,
	): String {
		val locale = key.locale
		val string = getTranslatedString(key)
		val formatter = MessageFormat(string, locale)

		return formatter.format(replacements)
	}

	private fun ResourceBundle.getStringOrNull(key: String): String? {
		return try {
			getString(key)
		} catch (_: MissingResourceException) {
			null
		}
	}

	private object Control : ResourceBundle.Control(), KordExKoinComponent {
		val builder: ExtensibleBotBuilder by inject()

		override fun getFormats(baseName: String?): MutableList<String> {
			if (baseName == null) {
				throw NullPointerException()
			}

			return FORMAT_PROPERTIES
		}

		override fun getFallbackLocale(baseName: String?, locale: Locale?): Locale? {
			if (baseName == null) {
				throw NullPointerException()
			}

			return if (locale == builder.i18nBuilder.defaultLocale) {
				null
			} else {
				builder.i18nBuilder.defaultLocale
			}
		}
	}
}
