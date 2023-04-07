/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.usagelimits.ratelimits

import com.kotlindiscord.kord.extensions.usagelimits.cooldowns.CooldownHistory
import com.kotlindiscord.kord.extensions.usagelimits.removeSmaller
import kotlinx.datetime.Instant
import java.util.*

/**
 * Default implementation of [CooldownHistory]
 *
 * Uses linkedLists internally because we are only interested in the total size and update the list frequently.
 */
public open class DefaultCooldownHistory : CooldownHistory {

    override var crossedCooldowns: LinkedList<Instant> = LinkedList()

    override fun removeExpiredCooldownHits(cutoffTime: Instant) {
        crossedCooldowns.removeSmaller(cutoffTime)
    }

    override fun addCooldownHit(moment: Instant) {
        crossedCooldowns.add(moment)
    }
}
