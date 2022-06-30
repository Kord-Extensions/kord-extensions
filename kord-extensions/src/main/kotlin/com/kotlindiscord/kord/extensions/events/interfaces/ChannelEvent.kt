/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.events.interfaces

import dev.kord.core.behavior.channel.ChannelBehavior
import dev.kord.core.entity.channel.Channel

/** Generic interface for custom events that can contain channel behaviors. Mostly used by checks. **/
public interface ChannelEvent {
    /** The channel behavior for this event, if any. **/
    public val channel: ChannelBehavior?

    /** Get a generic Channel object, or throw if one can't be retrieved. **/
    public suspend fun getChannel(): Channel

    /** Get a generic Channel object, or return null if one can't be retrieved. **/
    public suspend fun getChannelOrNull(): Channel?
}

/** Get a channel object of the given type, or throw if one can't be retrieved or cast. **/
public suspend inline fun <reified T : Channel> ChannelEvent.getChannelOf(): T =
    getChannel() as T

/** Get a channel object of the given type, or return null if one can't be retrieved or cast. **/
public suspend inline fun <reified T : Channel> ChannelEvent.getChannelOfOrNull(): T? =
    getChannelOrNull() as? T
