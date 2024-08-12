/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.modules.web.core.backend.routes

import dev.kordex.modules.web.core.backend.errors.MethodNotAllowedError
import dev.kordex.modules.web.core.backend.errors.error
import dev.kordex.modules.web.core.backend.routes.utils.allow
import io.ktor.server.application.*

@Suppress("StringLiteralDuplication")
public abstract class Route(public val extension: String) {
	public abstract val path: String

	public open suspend fun beforeRequest(verb: Verb, call: ApplicationCall): Boolean =
		call.allow()

	public open suspend fun delete(call: ApplicationCall) {
		call.error(MethodNotAllowedError)
	}

	public open suspend fun get(call: ApplicationCall) {
		call.error(MethodNotAllowedError)
	}

	public open suspend fun head(call: ApplicationCall) {
		call.error(MethodNotAllowedError)
	}

	public open suspend fun options(call: ApplicationCall) {
		call.error(MethodNotAllowedError)
	}

	public open suspend fun patch(call: ApplicationCall) {
		call.error(MethodNotAllowedError)
	}

	public open suspend fun post(call: ApplicationCall) {
		call.error(MethodNotAllowedError)
	}

	public open suspend fun put(call: ApplicationCall) {
		call.error(MethodNotAllowedError)
	}
}
