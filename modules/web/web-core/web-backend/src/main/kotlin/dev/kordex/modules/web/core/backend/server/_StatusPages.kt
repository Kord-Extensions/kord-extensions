/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.kordex.modules.web.core.backend.server

import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*

public fun WebServer.configureStatusPages(app: Application) {
	app.install(StatusPages) {
		// TODO: Error handling, etc
	}
}
