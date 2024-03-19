/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.modules.extra.phishing

import com.kotlindiscord.kord.extensions.builders.ExtensibleBotBuilder

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
