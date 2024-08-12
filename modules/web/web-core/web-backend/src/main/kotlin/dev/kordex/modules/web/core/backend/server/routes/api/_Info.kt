/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.modules.web.core.backend.server.routes.api

import dev.kordex.modules.web.core.backend.config.WebServerConfig
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

public fun Route.apiInfo(config: WebServerConfig) {
	val siteInfo by lazy {
		SiteInfo(
			devMode = config.devMode,
			title = config.siteTitle
		)
	}

	route("/info") {
		get("/site") {
			call.respond(siteInfo)
		}
	}
}

@Serializable
private data class SiteInfo(
	val devMode: Boolean,
	val title: String,
)
