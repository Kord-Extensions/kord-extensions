package com.kotlindiscord.kord.extensions.usagelimits.ratelimits

/** default example implementation of [UsageHistory]. **/
public class UsageHistoryImpl : UsageHistory {
    override val usages: MutableList<Long> = ArrayList()
    override var crossedLimits: MutableList<Long> = ArrayList()
    override var rateLimitState: Boolean = false
    override var crossedCooldowns: MutableList<Long> = ArrayList()
}
