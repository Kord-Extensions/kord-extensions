/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.kordex.extra.web.pages.navigation

import dev.kordex.extra.web.types.Identifier

public class ExtensionNavigation(
	public val extension: String,
	public val icon: Identifier,

	public val setup: ExtensionNavigation.() -> Unit
) {
	public val navigation: MutableList<NavigationItem> = mutableListOf()

	public fun navigation(body: NavigationItem.Builder.() -> Unit): NavigationItem {
		val builder = NavigationItem.Builder()
		body(builder)

		val item = builder.build()
		navigation.add(item)

		return item
	}
}
