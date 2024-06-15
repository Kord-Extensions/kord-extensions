/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.kordex.extra.web.utils

import com.kotlindiscord.kord.extensions.builders.ExtensibleBotBuilder
import dev.kordex.extra.web.WebExtension
import dev.kordex.extra.web.config.WebServerConfig

public fun ExtensibleBotBuilder.ExtensionsBuilder.web(builder: WebServerConfig.() -> Unit) {
	val config = WebServerConfig()

	builder(config)

	add { WebExtension(config) }
}
