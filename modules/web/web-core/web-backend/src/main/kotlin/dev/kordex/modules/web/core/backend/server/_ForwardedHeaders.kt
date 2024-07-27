/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.kordex.modules.web.core.backend.server

import dev.kordex.modules.web.core.backend.config.ForwardedHeaderMode
import dev.kordex.modules.web.core.backend.config.ForwardedHeaderStrategy
import io.ktor.server.application.*
import io.ktor.server.plugins.forwardedheaders.*

public fun WebServer.configureForwardedHeaders(app: Application) {
	when (config.reverseProxy.headerMode) {
		ForwardedHeaderMode.None -> {}

		ForwardedHeaderMode.Forwarded -> app.install(ForwardedHeaders) {
			when (val s = config.reverseProxy.headerStrategy) {
				ForwardedHeaderStrategy.First -> useFirstValue()
				ForwardedHeaderStrategy.Last -> useLastValue()

				is ForwardedHeaderStrategy.SkipKnown -> skipKnownProxies(s.known)
				is ForwardedHeaderStrategy.SkipLast -> skipLastProxies(s.number)

				is ForwardedHeaderStrategy.Custom -> extractValue(s.block)

				is ForwardedHeaderStrategy.XCustom -> error("Use the `Custom` strategy in `Forwarded` mode.")
			}
		}

		ForwardedHeaderMode.XForwarded -> app.install(XForwardedHeaders) {
			when (val s = config.reverseProxy.headerStrategy) {
				ForwardedHeaderStrategy.First -> useFirstProxy()
				ForwardedHeaderStrategy.Last -> useLastProxy()

				is ForwardedHeaderStrategy.SkipKnown -> skipKnownProxies(s.known)
				is ForwardedHeaderStrategy.SkipLast -> skipLastProxies(s.number)

				is ForwardedHeaderStrategy.XCustom -> extractEdgeProxy(s.block)

				is ForwardedHeaderStrategy.Custom -> error("Use the `XCustom` strategy in `XForwarded` mode.")
			}
		}
	}
}
