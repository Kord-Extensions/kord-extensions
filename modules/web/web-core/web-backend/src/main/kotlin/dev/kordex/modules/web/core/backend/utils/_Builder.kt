/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.modules.web.core.backend.utils

import dev.kordex.core.builders.ExtensionsBuilder
import dev.kordex.modules.web.core.backend.WebExtension
import dev.kordex.modules.web.core.backend.config.WebServerConfig

public fun ExtensionsBuilder.web(builder: WebServerConfig.() -> Unit) {
	val config = WebServerConfig()

	builder(config)

	add { WebExtension(config) }
}
