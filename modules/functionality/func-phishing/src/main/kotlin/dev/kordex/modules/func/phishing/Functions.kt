/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.modules.func.phishing

import dev.kordex.core.builders.ExtensibleBotBuilder

/**
 * Add the phishing extension to the bot with the default configuration.
 */
fun ExtensibleBotBuilder.ExtensionsBuilder.extPhishing() {
	val settings = ExtPhishingBuilder()

	settings.validate()

	add { PhishingExtension(settings) }
}

/**
 * Add the phishing extension to the bot with a customised configuration.
 */
inline fun ExtensibleBotBuilder.ExtensionsBuilder.extPhishing(builder: ExtPhishingBuilder.() -> Unit) {
	val settings = ExtPhishingBuilder()

	builder(settings)

	settings.validate()

	add { PhishingExtension(settings) }
}
