/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.modules.web.core.backend.config

import io.ktor.server.plugins.*
import io.ktor.server.plugins.forwardedheaders.*

public sealed interface ForwardedHeaderStrategy {
	public data object First : ForwardedHeaderStrategy
	public data object Last : ForwardedHeaderStrategy

	public data class SkipKnown(public val known: List<String>) :
		ForwardedHeaderStrategy

	public data class SkipLast(public val number: Int) :
		ForwardedHeaderStrategy

	public data class Custom(
		public val block: (MutableOriginConnectionPoint, List<ForwardedHeaderValue>) -> Unit,
	) : ForwardedHeaderStrategy

	public data class XCustom(
		public val block: (MutableOriginConnectionPoint, XForwardedHeaderValues) -> Unit,
	) : ForwardedHeaderStrategy
}
