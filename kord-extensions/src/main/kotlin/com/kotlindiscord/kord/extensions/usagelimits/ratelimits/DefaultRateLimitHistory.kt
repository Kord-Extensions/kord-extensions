/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.usagelimits.ratelimits

import com.kotlindiscord.kord.extensions.usagelimits.removeSmaller
import kotlinx.datetime.Instant
import java.util.*

/**
 * Default implementation of [RateLimitHistory]
 *
 * Uses linkedLists internally because we are only interested in the total size and update the list frequently.
 */
public class DefaultRateLimitHistory : RateLimitHistory {

    override var usages: LinkedList<Instant> = LinkedList()
    override var rateLimitState: Boolean = false
    override val rateLimitHits: LinkedList<Instant> = LinkedList()

    override fun removeExpiredUsages(cutoffTime: Instant) {
        usages.removeSmaller(cutoffTime)
    }

    override fun removeExpiredRateLimitHits(cutoffTime: Instant) {
        rateLimitHits.removeSmaller(cutoffTime)
    }

    override fun addUsage(moment: Instant) {
        usages.add(moment)
    }

    override fun addRateLimitHit(moment: Instant) {
        rateLimitHits.add(moment)
    }
}
