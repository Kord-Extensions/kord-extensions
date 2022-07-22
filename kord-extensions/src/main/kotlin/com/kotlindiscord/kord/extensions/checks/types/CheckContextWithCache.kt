/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.checks.types

import com.kotlindiscord.kord.extensions.utils.MutableStringKeyedMap
import dev.kord.core.event.Event
import java.util.*

/**
 * A check context that provides a cache map, specifically meant for use in event, component and command contexts
 * that provide a cache map for the lifecycle of the handler.
 *
 * @property cache Cache map, where you can store data you retrieve as part of your checks for use in later checks,
 * or your action block.
 */
public class CheckContextWithCache<out T : Event>(
    event: T,
    locale: Locale,

    public val cache: MutableStringKeyedMap<Any>,
) : CheckContext<T>(event, locale)
