/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.events.interfaces

import dev.kord.core.behavior.MemberBehavior
import dev.kord.core.entity.Member

/**
 * Generic interface for custom events that can contain member behaviors. Mostly used by checks.
 *
 * Using this interface also implies [GuildEvent] and [UserEvent], as both types of object can be retrieved from a
 * member object.
 */
public interface MemberEvent : GuildEvent, UserEvent {
	/** The member behavior for this event, if any. **/
	public val member: MemberBehavior?

	/** Get a Member object, or throw if one can't be retrieved. **/
	public suspend fun getMember(): Member

	/** Get a Member object, or return null if one can't be retrieved. **/
	public suspend fun getMemberOrNull(): Member?
}
