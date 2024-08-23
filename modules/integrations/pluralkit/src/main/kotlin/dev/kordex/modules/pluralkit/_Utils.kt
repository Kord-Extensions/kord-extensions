/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.modules.pluralkit

import dev.kordex.core.builders.ExtensionsBuilder
import dev.kordex.modules.pluralkit.config.PKConfigBuilder

/** Set up and add the PluralKit extension to your bot, using the default configuration. **/
fun ExtensionsBuilder.extPluralKit() {
	add { PKExtension(PKConfigBuilder()) }
}

/** Set up and add the PluralKit extension to your bot. **/
fun ExtensionsBuilder.extPluralKit(body: PKConfigBuilder.() -> Unit) {
	val builder = PKConfigBuilder()

	body(builder)

	add { PKExtension(builder) }
}
