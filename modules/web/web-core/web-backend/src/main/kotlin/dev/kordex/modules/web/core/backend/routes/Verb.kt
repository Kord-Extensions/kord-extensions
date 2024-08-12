/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.modules.web.core.backend.routes

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
