/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
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
