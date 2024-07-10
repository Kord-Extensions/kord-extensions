/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.kordex.extra.web.pages.navigation

import dev.kordex.extra.web.types.Identifier
import kotlinx.serialization.Serializable

@Serializable
public data class NavigationItem(
	public val name: String,
	public val icon: Identifier,
	public val page: String?,  // TODO: Page objects?
	public val children: MutableList<NavigationItem> = mutableListOf(),
) {
	public class Builder {
		public lateinit var name: String
		public lateinit var icon: Identifier
		public lateinit var page: String

		public var children: MutableList<NavigationItem> = mutableListOf()

		public fun build(): NavigationItem =
			NavigationItem(
				name = name,
				icon = icon,

				page = if (this::page.isInitialized) {
					page
				} else {
					null
				},

				children = children
			)

		public fun child(body: Builder.() -> Unit): NavigationItem {
			val builder = Builder()
			body(builder)

			val item = builder.build()
			children.add(item)

			return item
		}
	}
}
