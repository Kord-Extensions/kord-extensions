/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
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
