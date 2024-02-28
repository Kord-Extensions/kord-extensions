/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.events.interfaces

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
