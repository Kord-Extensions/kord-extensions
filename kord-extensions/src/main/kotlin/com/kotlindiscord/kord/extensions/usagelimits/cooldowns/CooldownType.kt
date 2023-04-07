/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.usagelimits.cooldowns

import com.kotlindiscord.kord.extensions.usagelimits.DiscriminatingContext
import kotlinx.datetime.Instant

/**
 * Used to implement different cooldown behaviours.
 */
public interface CooldownType {

    /** Gets the moment at which the cooldown will end or has ended. **/
    public fun getCooldown(context: DiscriminatingContext): Instant

    /** Sets the future moment at which the cooldown will end. **/
    public fun setCooldown(context: DiscriminatingContext, until: Instant)

    /** Gets the cooldownHistory from this cooldownType using [DiscriminatingContext] properties. **/
    public fun getCooldownUsageHistory(context: DiscriminatingContext): CooldownHistory

    /** Sets the cooldownHistory for this cooldownType using [DiscriminatingContext] properties. **/
    public fun setCooldownUsageHistory(context: DiscriminatingContext, usageHistory: CooldownHistory)
}
