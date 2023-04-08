/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.usagelimits.cooldowns

import kotlinx.datetime.Instant

/** Offers storing, retrieving and modifying cooldown history data. **/
public interface CooldownHistory {

    /** tracks moments in time when cooldowns were hit. **/
    public val cooldownHits: List<Instant>

    /** CrossedCooldown moments before [cutoffTime] will be removed from the usageHistory. **/
    public fun removeExpiredCooldownHits(cutoffTime: Instant)

    /** Adds a crossedCooldown moment to the usageHistory. **/
    public fun addCooldownHit(moment: Instant)
}
