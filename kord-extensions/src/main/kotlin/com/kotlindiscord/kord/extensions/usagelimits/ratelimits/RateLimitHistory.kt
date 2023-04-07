/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.usagelimits.ratelimits

import com.kotlindiscord.kord.extensions.usagelimits.UsageHistory
import kotlinx.datetime.Instant

/**
 * Offers storing, retrieving and modifying rateLimit history data.
 */
public interface RateLimitHistory : UsageHistory {

    /** tracks moments in time when rateLimits were hit. **/
    public val rateLimitHits: List<Instant>

    /** true when rate-limited. **/
    public val rateLimitState: Boolean

    /** RateLimitHit moments before [cutoffTime] will be removed from the usageHistory. **/
    public fun removeExpiredRateLimitHits(cutoffTime: Instant)

    /** Adds a rateLimitHit moment to the usageHistory. **/
    public fun addRateLimitHit(moment: Instant)
}
