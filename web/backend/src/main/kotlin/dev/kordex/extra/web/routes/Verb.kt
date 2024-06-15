/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.kordex.extra.web.routes

import io.ktor.http.*

public sealed class Verb(public val method: HttpMethod) {
	public data object DELETE : Verb(HttpMethod.Delete)
	public data object GET : Verb(HttpMethod.Get)
	public data object HEAD : Verb(HttpMethod.Head)
	public data object OPTIONS : Verb(HttpMethod.Options)
	public data object PATCH : Verb(HttpMethod.Patch)
	public data object POST : Verb(HttpMethod.Post)
	public data object PUT : Verb(HttpMethod.Put)
}
