/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.usagelimits.ratelimits

import com.kotlindiscord.kord.extensions.commands.Command
import com.kotlindiscord.kord.extensions.usagelimits.DiscriminatingContext

/**
 * Provides ratelimit configuration info.
 */
public interface RateLimitProvider {

    /** @return Used rateLimit types from the contexts and global settings. **/
    public suspend fun getRateLimitTypes(
        command: Command?,
        context: DiscriminatingContext,
    ): Set<RateLimitType>

    /** @returns The most applicable [RateLimit] based on the contexts and [type]. **/
    public suspend fun getRateLimit(
        command: Command,
        context: DiscriminatingContext,
        type: RateLimitType,
    ): RateLimit
}
