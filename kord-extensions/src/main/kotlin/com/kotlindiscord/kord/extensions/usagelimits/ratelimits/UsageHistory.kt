package com.kotlindiscord.kord.extensions.usagelimits.ratelimits

/** Interface that provides variables that hold usage history information about command usage and rate-limits. **/
public interface UsageHistory {

    /** tracks moments in time when actions were used. **/
    public val usages: MutableList<Long>

    /** tracks moments in time when limits were crossed. **/
    public var crossedLimits: MutableList<Long>

    /** tracks moments in time when cooldowns were hit. **/
    public var crossedCooldowns: MutableList<Long>

    /** true when rate-limited. **/
    public var rateLimitState: Boolean
}
