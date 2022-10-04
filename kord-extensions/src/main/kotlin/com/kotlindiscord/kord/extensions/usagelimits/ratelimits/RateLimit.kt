package com.kotlindiscord.kord.extensions.usagelimits.ratelimits

import kotlin.time.Duration

/**
 * Represents a rateLimit.
 **/
public data class RateLimit(
    /** Whether this rateLimit is active or not. **/
    val enabled: Boolean,
    /** The limit before the rateLimit is hit. **/
    val limit: Long,
    /** Duration of the rateLimit. **/
    val duration: Duration
) {
    public companion object {
        /** @return a class with a disabled ratelimit. **/
        public fun disabled(): RateLimit = RateLimit(false, 0, Duration.ZERO)
    }
}
