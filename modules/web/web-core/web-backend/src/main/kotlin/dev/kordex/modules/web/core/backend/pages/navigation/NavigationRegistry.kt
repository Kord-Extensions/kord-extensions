/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.modules.web.core.backend.pages.navigation

public class NavigationRegistry {
	public val extensions: MutableMap<String, ExtensionNavigation> = mutableMapOf()

	public fun add(navigation: ExtensionNavigation) {
		if (navigation.extension in extensions) {
			error("Navigation for ${navigation.extension} is already registered.")
		}

		extensions[navigation.extension] = navigation
	}

	public fun remove(navigation: ExtensionNavigation): ExtensionNavigation? =
		remove(navigation.extension)

	public fun remove(extension: String): ExtensionNavigation? =
		extensions.remove(extension)

	public fun removeAll() {
		extensions.clear()
	}
}
