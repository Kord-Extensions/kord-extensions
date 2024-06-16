/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.kordex.extra.web.errors

import io.ktor.http.*

public val MethodNotAllowedError: WebError = WebError(
	"Method not allowed",
	HttpStatusCode.MethodNotAllowed
)
