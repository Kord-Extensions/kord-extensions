/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.usagelimits.ratelimits

import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.events.ApplicationCommandInvocationEvent
import com.kotlindiscord.kord.extensions.commands.events.ChatCommandInvocationEvent
import com.kotlindiscord.kord.extensions.commands.events.CommandInvocationEvent
import com.kotlindiscord.kord.extensions.usagelimits.DiscriminatingContext
import com.kotlindiscord.kord.extensions.usagelimits.UsageLimitType

/**
 * Abstraction that allows you to implement custom ratelimiting behaviour.
 * **/
public interface RateLimiter {

    /**
     * Checks if the command should not be run due to a rateLimit.
     * If so it should save the ratelimit hit and can give a response only if it is rate-limited.
     *
     * Mutates the associated [UsageHistory] of various [UsageLimitTypes][UsageLimitType]
     *
     * @return true if the command is rate-limited, false if not rate-limited
     */
    public suspend fun checkCommandRatelimit(context: DiscriminatingContext): Boolean

    /**
     * @return true if a ratelimit message should be sent, false otherwise
     */
    public suspend fun shouldSendMessage(usageHistory: UsageHistory, rateLimit: RateLimit, type: RateLimitType): Boolean

    /**
     * Should send a message in the discord channel where the command was used with information about what ratelimit
     * was hit and when the user can use the command again.
     * Can be adapted to also log information into a logChannel (not an implementation goal)
     *
     * @param context the [CommandContext] that caused this ratelimit hit
     * @param usageHistory the involved [UsageHistory]
     * @param rateLimit the involved [RateLimit]
     */
    public suspend fun sendRateLimitedMessage(
        context: DiscriminatingContext,
        type: RateLimitType,
        usageHistory: UsageHistory,
        rateLimit: RateLimit
    )

    /**
     * Wrapper function to convert invocationEvent into the required [DiscriminatingContext]
     * Checks if the chatCommand should not be run due to a rateLimit.
     * If so it should save the ratelimit hit and can give a response only if it is rate-limited.
     *
     * Mutates the associated [UsageHistory] of various [UsageLimitTypes][UsageLimitType]
     *
     * @return true if the command is rate-limited, false if not rate-limited
     */
    public suspend fun checkCommandRatelimit(invocationEvent: CommandInvocationEvent<*, *>): Boolean {
        val context = when (invocationEvent) {
            is ApplicationCommandInvocationEvent -> DiscriminatingContext(invocationEvent)
            is ChatCommandInvocationEvent -> DiscriminatingContext(invocationEvent)
        }
        return checkCommandRatelimit(context)
    }
}
