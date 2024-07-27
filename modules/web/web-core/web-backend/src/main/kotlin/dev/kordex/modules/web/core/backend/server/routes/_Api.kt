/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.kordex.modules.web.core.backend.server.routes

import dev.kordex.modules.web.core.backend.config.WebServerConfig
import dev.kordex.modules.web.core.backend.server.routes.api.apiInfo
import io.ktor.server.routing.*

public fun Routing.api(config: WebServerConfig) {
	route("/api") {
		apiInfo(config)
	}
}
