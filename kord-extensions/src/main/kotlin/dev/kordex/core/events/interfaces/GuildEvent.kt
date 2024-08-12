/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.events.interfaces

import dev.kord.core.behavior.GuildBehavior
import dev.kord.core.entity.Guild

/** Generic interface for custom events that can contain guild behaviors. Mostly used by checks. **/
public interface GuildEvent {
	/** The guild behavior for this event, if any. **/
	public val guild: GuildBehavior?

	/** Get a Guild object, or throw if one can't be retrieved. **/
	public suspend fun getGuild(): Guild

	/** Get a Guild object, or return null if one can't be retrieved. **/
	public suspend fun getGuildOrNull(): Guild?
}
