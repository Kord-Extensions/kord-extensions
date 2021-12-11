/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.events

import dev.kord.core.Kord
import dev.kord.core.event.Event
import org.koin.core.component.KoinComponent
import kotlin.coroutines.CoroutineContext

/**
 * Base interface for events fired by Kord Extensions.
 */
public interface KordExEvent : Event, KoinComponent {
    override val kord: Kord get() = getKoin().get()
    override val shard: Int get() = -1
    override val coroutineContext: CoroutineContext get() = kord.coroutineContext
}
