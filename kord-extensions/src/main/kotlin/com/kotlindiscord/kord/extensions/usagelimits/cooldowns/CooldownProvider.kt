/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.usagelimits.cooldowns

import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.usagelimits.DiscriminatingContext
import kotlin.time.Duration

/**
 * Provides cooldown configuration info.
 */
public interface CooldownProvider {

    /** @return Used cooldown types from the contexts and global settings. **/
    public suspend fun getCooldownTypes(
        commandContext: CommandContext?,
        context: DiscriminatingContext,
    ): Set<CooldownType>

    /** @return The longest cooldown [Duration] based on the contexts and [type]. **/
    public suspend fun getCooldown(
        commandContext: CommandContext,
        context: DiscriminatingContext,
        type: CooldownType,
    ): Duration
}
