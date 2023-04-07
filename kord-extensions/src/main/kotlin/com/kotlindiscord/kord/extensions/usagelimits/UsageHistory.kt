/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.usagelimits

import kotlinx.datetime.Instant

/**
 * Offers storing, retrieving and modifying command usage history data.
 */
public interface UsageHistory {

    /** Tracks moments in time when actions were used. **/
    public val usages: List<Instant>

    /** Usage moments before [cutoffTime] will be removed from the usageHistory. **/
    public fun removeExpiredUsages(cutoffTime: Instant)

    /** Adds a usage moment to the usageHistory. **/
    public fun addUsage(moment: Instant)
}
