/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.events.interfaces

import dev.kord.core.behavior.MessageBehavior
import dev.kord.core.entity.Message

/** Generic interface for custom events that can contain message behaviors. Mostly used by checks. **/
public interface MessageEvent {
    /** The message behavior for this event, if any. **/
    public val message: MessageBehavior?

    /** Get a Message object, or throw if one can't be retrieved. **/
    public suspend fun getMessage(): Message

    /** Get a Message object, or return null if one can't be retrieved. **/
    public suspend fun getMessageOrNull(): Message?
}
