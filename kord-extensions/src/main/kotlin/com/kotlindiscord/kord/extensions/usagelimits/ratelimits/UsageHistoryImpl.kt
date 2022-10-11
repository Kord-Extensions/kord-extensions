/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.usagelimits.ratelimits

/** default example implementation of [UsageHistory]. **/
public class UsageHistoryImpl : UsageHistory {
    override val usages: MutableList<Long> = ArrayList()
    override var crossedLimits: MutableList<Long> = ArrayList()
    override var rateLimitState: Boolean = false
    override var crossedCooldowns: MutableList<Long> = ArrayList()
}
