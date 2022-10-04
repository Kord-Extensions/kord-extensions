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
