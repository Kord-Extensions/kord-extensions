/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.kordex.extra.web.config

public class WebServerConfig {
	public var devMode: Boolean = false

	@Suppress("MagicNumber")
	public var port: Int = 8080

	public var forwardedHeaderMode: ForwardedHeaderMode = ForwardedHeaderMode.None
	public var forwardedHeaderStrategy: ForwardedHeaderStrategy = ForwardedHeaderStrategy.First

	public lateinit var hostname: String
}
