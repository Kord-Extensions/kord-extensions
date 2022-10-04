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
