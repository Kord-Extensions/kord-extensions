/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.kordex.modules.pluralkit

import dev.kordex.core.builders.ExtensibleBotBuilder
import dev.kordex.modules.pluralkit.config.PKConfigBuilder

/** Set up and add the PluralKit extension to your bot, using the default configuration. **/
fun ExtensibleBotBuilder.ExtensionsBuilder.extPluralKit() {
	add { PKExtension(PKConfigBuilder()) }
}

/** Set up and add the PluralKit extension to your bot. **/
fun ExtensibleBotBuilder.ExtensionsBuilder.extPluralKit(body: PKConfigBuilder.() -> Unit) {
	val builder = PKConfigBuilder()

	body(builder)

	add { PKExtension(builder) }
}
