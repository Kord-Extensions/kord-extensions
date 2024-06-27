/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.kordex.extra.web.server.routes.api

import dev.kordex.extra.web.config.WebServerConfig
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
