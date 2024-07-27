/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
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
