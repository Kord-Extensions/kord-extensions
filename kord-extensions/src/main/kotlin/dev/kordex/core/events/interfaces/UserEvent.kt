/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.events.interfaces

import dev.kord.core.behavior.UserBehavior
import dev.kord.core.entity.User

/** Generic interface for custom events that can contain user behaviors. Mostly used by checks. **/
public interface UserEvent {
	/** The user behavior for this event, if any. **/
	public val user: UserBehavior?

	/** Get a User object, or throw if one can't be retrieved. **/
	public suspend fun getUser(): User

	/** Get a User object, or return null if one can't be retrieved. **/
	public suspend fun getUserOrNull(): User?
}
