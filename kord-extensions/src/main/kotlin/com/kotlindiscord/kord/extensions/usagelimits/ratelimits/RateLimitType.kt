/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.usagelimits.ratelimits

import com.kotlindiscord.kord.extensions.usagelimits.DiscriminatingContext

/**
 * Should be used to discriminate on situations and
 * save [usageHistories][UsageHistory] in different [rateLimitTypes][RateLimitType].
 * **/
public interface RateLimitType {
    /** Gets the usageHistory from this ratelimit type using [DiscriminatingContext] properties. **/
    public fun getUsageHistory(context: DiscriminatingContext): UsageHistory

    /** Sets the usageHistory for this ratelimit type using [DiscriminatingContext] properties. **/
    public fun setUsageHistory(context: DiscriminatingContext, usageHistory: UsageHistory)
}
