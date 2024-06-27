/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.kordex.extra.web.pages.navigation

import kotlinx.serialization.Serializable

@Serializable
public sealed class NavigationItem {
	public abstract val name: String
	public abstract val icon: String
	public abstract val iconOnly: Boolean
	public abstract val path: String?

	@Serializable
	public class WithChildren(
		override val name: String,
		override val path: String?,
		override val icon: String,
		override val iconOnly: Boolean = false,

		public val children: List<NavigationItem> = listOf(),
	) : NavigationItem()

	@Serializable
	public class Single(
		override val name: String,
		override val path: String?,
		override val icon: String,
		override val iconOnly: Boolean = false,
	) : NavigationItem()
}
