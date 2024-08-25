/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.builders.about

import dev.kord.rest.builder.message.MessageBuilder
import dev.kordex.core.i18n.TranslationsProvider
import dev.kordex.core.koin.KordExKoinComponent
import org.koin.core.component.inject
import java.util.Locale
import kotlin.getValue

internal typealias SectionBuilder = suspend MessageBuilder.(locale: Locale) -> Unit

public class Section(public val name: String, public val description: String) : KordExKoinComponent {
	public val translations: TranslationsProvider by inject()

	public var ephemeral: Boolean? = null
	public var translationBundle: String? = null

	public lateinit var builder: SectionBuilder

	public fun message(builder: SectionBuilder) {
		this.builder = builder
	}

	public fun translate(key: String, locale: Locale, replacements: Array<Any?> = arrayOf()): String =
		translations.translate(key = key, bundleName = translationBundle, locale = locale, replacements = replacements)

	public fun validate() {
		if (!::builder.isInitialized) {
			error("No builder provided - use the `message` DSL function to add one.")
		}
	}
}
