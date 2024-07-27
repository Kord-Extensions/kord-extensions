/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.kordex.core.utils

/** Object with easy access to unusual mentionable sections of Discord. **/
public object Mentions {
	/** A clickable mention that takes you to this server's home tab. **/
	public const val GuildHome: String = "<id:home>"

	/** A clickable mention that takes you to this server's "browse channels" tab. **/
	public const val GuildBrowseChannels: String = "<id:browse>"

	/** A clickable mention that takes you to this server's "customize community"/onboarding tab. **/
	public const val GuildCustomizeCommunity: String = "<id:customize>"
}
