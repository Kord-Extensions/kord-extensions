/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.modules.web.core.backend.pages.navigation

import dev.kordex.modules.web.core.backend.types.Identifier

public class ExtensionNavigation(
	public val extension: String,
	public val icon: Identifier,

	public val setup: ExtensionNavigation.() -> Unit,
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
