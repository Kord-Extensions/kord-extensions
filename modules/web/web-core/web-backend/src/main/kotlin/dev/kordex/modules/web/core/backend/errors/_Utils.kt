/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.modules.web.core.backend.errors

import io.ktor.server.application.*
import io.ktor.server.response.*

public suspend fun ApplicationCall.error(error: WebError) {
	this.respond(error.statusCode, error)
}
