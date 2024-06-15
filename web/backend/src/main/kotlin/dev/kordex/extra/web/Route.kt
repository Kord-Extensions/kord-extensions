/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.kordex.extra.web

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*

@Suppress("StringLiteralDuplication")
public abstract class Route(public val extension: String) {
	public abstract val path: String

	public open suspend fun delete(call: ApplicationCall) {
		call.response.header("Content-Type", "application/json")
		call.respond(HttpStatusCode.MethodNotAllowed, mutableMapOf("error" to "Method not allowed"))
	}

	public open suspend fun get(call: ApplicationCall) {
		call.response.header("Content-Type", "application/json")
		call.respond(HttpStatusCode.MethodNotAllowed, mutableMapOf("error" to "Method not allowed"))
	}

	public open suspend fun head(call: ApplicationCall) {
		call.response.header("Content-Type", "application/json")
		call.respond(HttpStatusCode.MethodNotAllowed, mutableMapOf("error" to "Method not allowed"))
	}

	public open suspend fun options(call: ApplicationCall) {
		call.response.header("Content-Type", "application/json")
		call.respond(HttpStatusCode.MethodNotAllowed, mutableMapOf("error" to "Method not allowed"))
	}

	public open suspend fun patch(call: ApplicationCall) {
		call.response.header("Content-Type", "application/json")
		call.respond(HttpStatusCode.MethodNotAllowed, mutableMapOf("error" to "Method not allowed"))
	}

	public open suspend fun post(call: ApplicationCall) {
		call.response.header("Content-Type", "application/json")
		call.respond(HttpStatusCode.MethodNotAllowed, mutableMapOf("error" to "Method not allowed"))
	}

	public open suspend fun put(call: ApplicationCall) {
		call.response.header("Content-Type", "application/json")
		call.respond(HttpStatusCode.MethodNotAllowed, mutableMapOf("error" to "Method not allowed"))
	}
}
