/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.usagelimits.cooldowns

import com.kotlindiscord.kord.extensions.usagelimits.DiscriminatingContext

/**
 * Should be used to discriminate on situations and
 * save cooldowns in different [cooldownTypes][CooldownType].
 * **/
public interface CooldownType {
    /** Gets the moment(in epoch millis) at which the cooldown will end or has ended. **/
    public fun getCooldown(context: DiscriminatingContext): Long

    /** Sets the future moment(in epoch millis) at which the cooldown will end. **/
    public fun setCooldown(context: DiscriminatingContext, until: Long)
}
