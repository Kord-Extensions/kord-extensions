/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.usagelimits.ratelimits

import com.kotlindiscord.kord.extensions.usagelimits.CachedUsageLimitType
import com.kotlindiscord.kord.extensions.usagelimits.DiscriminatingContext

/**
 * RatelimitProvider interface provides functions to get the [RateLimit] in a situation for a specific
 * [CachedUsageLimitType].
 * **/
public interface RateLimitProvider {
    /** Function returns a [RateLimit] based on the [context] and [type]. **/
    public suspend fun getRateLimit(context: DiscriminatingContext, type: CachedUsageLimitType): RateLimit
}
