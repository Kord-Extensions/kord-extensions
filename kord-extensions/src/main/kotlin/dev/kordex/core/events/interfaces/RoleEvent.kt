/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.events.interfaces

import dev.kord.core.behavior.RoleBehavior
import dev.kord.core.entity.Role

/** Generic interface for custom events that can contain role behaviors. Mostly used by checks. **/
public interface RoleEvent {
	/** The role behavior for this event, if any. **/
	public val role: RoleBehavior?

	/** Get a Role object, or throw if one can't be retrieved. **/
	public suspend fun getRole(): Role

	/** Get a Role object, or return null if one can't be retrieved. **/
	public suspend fun getRoleOrNull(): Role?
}
