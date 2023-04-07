/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.usagelimits.ratelimits

import com.kotlindiscord.kord.extensions.usagelimits.DiscriminatingContext

/**
 * Used to implement different ratelimit behaviours.
 */
public interface RateLimitType {

    /** Gets the rateLimitHistory from this rateLimitType using [DiscriminatingContext] properties. **/
    public fun getRateLimitUsageHistory(context: DiscriminatingContext): RateLimitHistory

    /** Sets the rateLimitHistory for this rateLimitType using [DiscriminatingContext] properties. **/
    public fun setRateLimitUsageHistory(context: DiscriminatingContext, rateLimitHistory: RateLimitHistory)
}
