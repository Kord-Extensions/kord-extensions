/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.usagelimits.ratelimits

import java.util.LinkedList

/**
 * Default example implementation of [UsageHistory].
 * Uses linkedLists internally because we are only interested in the total size and update the list frequently.
 * **/
public open class UsageHistoryImpl : UsageHistory {

    override var usages: LinkedList<Long> = LinkedList()
    override var crossedLimits: LinkedList<Long> = LinkedList()
    override var rateLimitState: Boolean = false
    override var crossedCooldowns: LinkedList<Long> = LinkedList()

    override fun removeExpiredUsages(cutoffTime: Long) {
        usages.removeSmaller(cutoffTime)
    }

    override fun removeExpiredCrossedLimits(cutoffTime: Long) {
        crossedLimits.removeSmaller(cutoffTime)
    }

    override fun removeExpiredCrossedCooldowns(cutoffTime: Long) {
        crossedCooldowns.removeSmaller(cutoffTime)
    }

    override fun addUsage(moment: Long) {
        usages.add(moment)
    }

    override fun addCrossedLimit(moment: Long) {
        crossedLimits.add(moment)
    }

    override fun addCrossedCooldown(moment: Long) {
        crossedCooldowns.add(moment)
    }

    private fun LinkedList<Long>.removeSmaller(cutoffTime: Long) {
        val iterator = this.iterator()
        while (iterator.hasNext()) {
            if (iterator.next() < cutoffTime) {
                iterator.remove()
            } else {
                break
            }
        }
    }
}
