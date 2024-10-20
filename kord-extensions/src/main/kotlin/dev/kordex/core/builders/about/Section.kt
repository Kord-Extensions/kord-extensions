/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.builders.about

import dev.kord.rest.builder.message.MessageBuilder
import dev.kordex.core.i18n.toKey
import dev.kordex.core.i18n.types.Key
import dev.kordex.core.koin.KordExKoinComponent
import java.util.Locale
import kotlin.getValue

internal typealias SectionBuilder = suspend MessageBuilder.(locale: Locale) -> Unit

public class Section(public val name: Key, public val description: Key) : KordExKoinComponent {
	@Suppress("ClassOrdering")  // THIS IS RIGHT!
	public constructor(name: String, description: String) : this(name.toKey(), description.toKey())

	public var ephemeral: Boolean? = null

	public lateinit var builder: SectionBuilder

	public fun message(builder: SectionBuilder) {
		this.builder = builder
	}

	public fun validate() {
		if (!::builder.isInitialized) {
			error("No builder provided - use the `message` DSL function to add one.")
		}
	}
}
