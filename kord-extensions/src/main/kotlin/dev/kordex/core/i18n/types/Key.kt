/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.i18n.types

import dev.kordex.core.i18n.TranslationsProvider
import dev.kordex.core.i18n.serializers.LocaleSerializer
import dev.kordex.core.utils.getKoin
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import java.util.Locale
import kotlin.collections.asList
import kotlin.collections.plus

public typealias PostProcessor = Key.(translation: String) -> String

@Serializable
public data class Key(
	public val key: String,
	public val bundle: Bundle? = null,

	@Serializable(with = LocaleSerializer::class)
	public val locale: Locale? = null,

	public val presetPlaceholderPosition: PlaceholderPosition = PlaceholderPosition.FIRST,
	public val translateNestedKeys: Boolean = true,

	@Transient
	public val ordinalPlaceholders: List<Any?> = listOf(),

	@Transient
	public val namedPlaceholders: Map<String, Any?> = mapOf(),

	@Transient
	public val postProcessors: List<PostProcessor> = listOf()
) {
	private val translations: TranslationsProvider by lazy {
		getKoin().get()
	}

	public fun filterOrdinalPlaceholders(body: (Any?) -> Boolean): Key =
		copy(ordinalPlaceholders = ordinalPlaceholders.filter(body))

	public fun filterNamedPlaceholders(body: (Map.Entry<String, Any?>) -> Boolean): Key =
		copy(namedPlaceholders = namedPlaceholders.filter(body))

	public fun withPostProcessor(processor: PostProcessor): Key =
		copy(postProcessors = postProcessors + processor)

	public fun withPostProcessors(processors: Collection<PostProcessor>): Key =
		copy(postProcessors = postProcessors + processors)

	public fun filterPostProcessors(body: (PostProcessor) -> Boolean): Key =
		copy(postProcessors = postProcessors.filter(body))

	public fun withOrdinalPlaceholders(vararg placeholders: Any?): Key {
		if (namedPlaceholders.isNotEmpty()) {
			error(
				"This Key object already contains named placeholders. " +
					"You may only use one type of placeholder at once."
			)
		}

		return copy(ordinalPlaceholders = ordinalPlaceholders + placeholders.toList().toTypedArray())
	}

	public fun withNamedPlaceholders(vararg placeholders: Pair<String, Any?>): Key =
		copy(namedPlaceholders = namedPlaceholders + placeholders)

	public fun withNamedPlaceholders(placeholders: Map<String, Any?>): Key =
		copy(namedPlaceholders = placeholders + placeholders)

	public fun withPresetPlaceholderPosition(position: PlaceholderPosition): Key =
		copy(presetPlaceholderPosition = position)

	public fun withTranslateNestedKeys(option: Boolean): Key =
		copy(translateNestedKeys = option)

	public fun withBundle(bundle: Bundle?, overwrite: Boolean = true): Key =
		if (bundle == this.bundle) {
			this
		} else if (this.bundle == null || overwrite) {
			copy(bundle = bundle)
		} else {
			this
		}

	public fun withLocale(locale: Locale?, overwrite: Boolean = true): Key =
		if (locale == this.locale) {
			this
		} else if (this.locale == null || overwrite) {
			copy(locale = locale)
		} else {
			this
		}

	public fun withBoth(
		bundle: Bundle?,
		locale: Locale?,
		overwriteBundle: Boolean = true,
		overwriteLocale: Boolean = true,
	): Key {
		val newBundle = if (this.bundle == null || overwriteBundle) {
			bundle
		} else {
			this.bundle
		}

		val newLocale = if (this.locale == null || overwriteLocale) {
			locale
		} else {
			this.locale
		}

		if (newBundle == this.bundle && newLocale == this.locale) {
			this
		}

		return copy(bundle = newBundle, locale = newLocale)
	}

	public fun withoutOrdinalPlaceholders(): Key =
		copy(ordinalPlaceholders = listOf())

	public fun withoutNamedPlaceholders(): Key =
		copy(namedPlaceholders = mapOf())

	public fun withoutBundle(): Key =
		copy(bundle = null)

	public fun withoutLocale(): Key =
		copy(locale = null)

	public fun withoutBoth(): Key =
		copy(bundle = null, locale = null)

	public fun translate(vararg replacements: Any?): String =
		if (replacements.isNotEmpty() || ordinalPlaceholders.isNotEmpty()) {
			translateArray(replacements.toList().toTypedArray())
		} else {
			translateNamed()
		}

	public fun translateArray(replacements: Array<Any?>): String {
		val allReplacements = when (presetPlaceholderPosition) {
			PlaceholderPosition.FIRST -> ordinalPlaceholders + replacements
			PlaceholderPosition.LAST -> replacements.asList() + ordinalPlaceholders
		}.map {
			if (translateNestedKeys && it is Key) {
				it
					.withBundle(bundle, false)
					.withLocale(locale, false)
					.translate()
			} else {
				it
			}
		}

		return postProcess(translations.translate(this, allReplacements.toTypedArray()))
	}

	public fun translateNamed(replacements: Map<String, Any?>): String {
		val allReplacements = when (presetPlaceholderPosition) {
			PlaceholderPosition.FIRST -> namedPlaceholders + replacements
			PlaceholderPosition.LAST -> replacements + namedPlaceholders
		}.mapValues {
			if (translateNestedKeys && it.value is Key) {
				(it.value as Key)
					.withBundle(bundle, false)
					.withLocale(locale, false)
					.translate()
			} else {
				it.value
			}
		}

		return postProcess(translations.translateNamed(this, allReplacements))
	}

	public fun translateNamed(vararg replacements: Pair<String, Any?>): String =
		translateNamed(replacements.toMap())

	public fun translateLocale(locale: Locale, vararg replacements: Any?): String =
		withLocale(locale).translate(*replacements)

	public fun translateArrayLocale(locale: Locale, replacements: Array<Any?>): String =
		withLocale(locale).translateArray(replacements)

	public fun translateNamedLocale(locale: Locale, replacements: Map<String, Any?>): String =
		withLocale(locale).translateNamed(replacements)

	public fun translateNamedLocale(locale: Locale, vararg replacements: Pair<String, Any?>): String =
		translateNamedLocale(locale, replacements.toMap())

	public fun postProcess(string: String): String {
		var result = string

		postProcessors.forEach {
			result = it.invoke(this, result)
		}

		return result
	}

	// Key "translation.key" (Bundle "name" / Locale "en_GB")
	override fun toString(): String =
		buildString {
			append("Key \"$key\"")

			if (bundle != null || locale != null) {
				append("(")

				if (bundle != null) {
					append(bundle)

					if (locale != null) {
						append(" / ")
					}
				}

				if (locale != null) {
					append("Locale \"${locale.toLanguageTag()}\"")
				}

				append(")")
			}
		}
}
