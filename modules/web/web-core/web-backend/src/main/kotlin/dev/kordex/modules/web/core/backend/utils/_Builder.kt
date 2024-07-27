/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.kordex.modules.web.core.backend.utils

import dev.kordex.core.builders.ExtensibleBotBuilder
import dev.kordex.modules.web.core.backend.WebExtension
import dev.kordex.modules.web.core.backend.config.WebServerConfig

public fun ExtensibleBotBuilder.ExtensionsBuilder.web(builder: WebServerConfig.() -> Unit) {
	val config = WebServerConfig()

	builder(config)

	add { WebExtension(config) }
}
