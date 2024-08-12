/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.modules.web.core.backend.server

import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*

public fun WebServer.configureStatusPages(app: Application) {
	app.install(StatusPages) {
		// TODO: Error handling, etc
	}
}
