/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.events

import com.kotlindiscord.kord.extensions.koin.KordExKoinComponent
import com.kotlindiscord.kord.extensions.utils.MutableStringKeyedMap
import dev.kord.common.annotation.KordPreview
import dev.kord.core.Kord
import dev.kord.core.event.Event

/**
 * Base interface for events fired by Kord Extensions.
 */
public interface KordExEvent : Event, KordExKoinComponent {
    override val kord: Kord get() = getKoin().get()
    override val shard: Int get() = -1

    @KordPreview
    override val customContext: MutableStringKeyedMap<Any>
        get() = mutableMapOf()
}
