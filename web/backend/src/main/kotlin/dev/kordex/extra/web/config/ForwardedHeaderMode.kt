/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.kordex.extra.web.config

public sealed interface ForwardedHeaderMode {
	public data object None : ForwardedHeaderMode
	public data object Forwarded : ForwardedHeaderMode
	public data object XForwarded : ForwardedHeaderMode
}
