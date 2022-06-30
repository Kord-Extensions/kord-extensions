/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.events.interfaces

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
