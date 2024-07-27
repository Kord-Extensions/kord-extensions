/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.kordex.modules.web.core.backend.errors

import io.ktor.server.application.*
import io.ktor.server.response.*

public suspend fun ApplicationCall.error(error: WebError) {
	this.respond(error.statusCode, error)
}
