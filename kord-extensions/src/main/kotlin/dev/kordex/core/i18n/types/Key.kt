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
import java.util.Locale

@Serializable
public data class Key(
	public val key: String,
	public val bundle: Bundle? = null,

	@Serializable(with = LocaleSerializer::class)
	public val locale: Locale? = null,
) {
	private val translations: TranslationsProvider by lazy {
		getKoin().get()
	}

	public fun withBundle(bundle: Bundle?, overwrite: Boolean = true): Key =
		if (this.bundle != null && !overwrite) {
			copy(bundle = bundle)
		} else {
			this
		}

	public fun withLocale(locale: Locale?, overwrite: Boolean = true): Key =
		if (this.locale != null && !overwrite) {
			copy(locale = locale)
		} else {
			this
		}

	public fun withoutBundle(): Key =
		copy(bundle = null)

	public fun withoutLocale(): Key =
		copy(locale = null)

	public fun translate(vararg replacements: Any?): String =
		translations.translate(this, replacements.toList().toTypedArray())

	public fun translateNamed(replacements: Map<String, Any?>): String =
		translations.translateNamed(this, replacements)

	public fun translateNamed(vararg replacements: Pair<String, Any?>): String =
		translateNamed(replacements.toMap())

	override fun toString(): String =
		buildString {
			append("Key $key")

			if (bundle != null || locale != null) {
				append("(")

				if (bundle != null) {
					append(bundle)

					if (locale != null) {
						append(" / ")
					}
				}

				if (locale != null) {
					append("${locale.toLanguageTag()}")
				}

				append(")")
			}
		}
}
