/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.modules.web.core.backend.pages.navigation

import dev.kordex.modules.web.core.backend.types.Identifier
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
