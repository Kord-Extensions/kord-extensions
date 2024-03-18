/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.modules.extra.pluralkit

import com.kotlindiscord.kord.extensions.builders.ExtensibleBotBuilder
import com.kotlindiscord.kord.extensions.modules.extra.pluralkit.config.PKConfigBuilder

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
