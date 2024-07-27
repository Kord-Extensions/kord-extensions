/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
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
