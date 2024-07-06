/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.kordex.extra.web.routes

import dev.kordex.extra.web.errors.MethodNotAllowedError
import dev.kordex.extra.web.errors.error
import dev.kordex.extra.web.routes.utils.allow
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
