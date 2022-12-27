/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.usagelimits.ratelimits

/** Interface that provides variables that hold usage history information about command usage and rate-limits. **/
public interface UsageHistory {

    /** tracks moments in time when actions were used. **/
    public val usages: List<Long>

    /** tracks moments in time when limits were crossed. **/
    public val crossedLimits: List<Long>

    /** tracks moments in time when cooldowns were hit. **/
    public val crossedCooldowns: List<Long>

    /** true when rate-limited. **/
    public val rateLimitState: Boolean

    /** CrossedCooldown moments before [cutoffTime] will be removed from the usageHistory. **/
    public fun removeExpiredCrossedCooldowns(cutoffTime: Long)

    /** Adds a crossedCooldown moment to the usageHistory. **/
    public fun addCrossedCooldown(moment: Long)

    /** Usage moments before [cutoffTime] will be removed from the usageHistory. **/
    public fun removeExpiredUsages(cutoffTime: Long)

    /** Adds a usage moment to the usageHistory. **/
    public fun addUsage(moment: Long)

    /** CrossedLimit moments before [cutoffTime] will be removed from the usageHistory. **/
    public fun removeExpiredCrossedLimits(cutoffTime: Long)

    /** Adds a crossedLimit moment to the usageHistory. **/
    public fun addCrossedLimit(moment: Long)
}
