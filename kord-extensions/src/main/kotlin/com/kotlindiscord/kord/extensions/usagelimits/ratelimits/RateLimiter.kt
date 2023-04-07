/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.usagelimits.ratelimits

import com.kotlindiscord.kord.extensions.commands.Command
import com.kotlindiscord.kord.extensions.commands.events.*
import com.kotlindiscord.kord.extensions.usagelimits.DiscriminatingContext

/**
 * A rateLimiter is responsible for checking if a command is rateLimited and sending a rateLimited message.
 * As well as updating the rateLimitHistories after a command execution.
 */
public interface RateLimiter {

    /**
     * Checks whether the command should be run.
     * This is called before the command is executed.
     *
     * @param command the [Command] that is being executed
     * @param context the [DiscriminatingContext] that caused this ratelimit hit
     *
     * @return true if the command is rateLimited, false if not rateLimited
     */
    public suspend fun checkCommandRatelimit(command: Command, context: DiscriminatingContext): Boolean

    /**
     * @return true if a rateLimit message should be sent, false otherwise
     */
    public suspend fun shouldSendMessage(
        usageHistory: RateLimitHistory,
        rateLimit: RateLimit,
        type: RateLimitType,
    ): Boolean

    /**
     * Sends a rateLimit message.
     *
     * @param context the [DiscriminatingContext] that caused this ratelimit hit
     * @param type the [RateLimitType] that was hit
     * @param rateLimitHistory the current [RateLimitHistory] for this [type]
     * @param rateLimit the involved [RateLimit]
     */
    public suspend fun sendRateLimitedMessage(
        context: DiscriminatingContext,
        type: RateLimitType,
        rateLimitHistory: RateLimitHistory,
        rateLimit: RateLimit,
    )

    /**
     * Checks whether the command should be run.
     *
     * @return true if the command is rateLimited, false if not rateLimited
     */
    public suspend fun checkCommandRatelimit(invocationEvent: CommandInvocationEvent<*, *>): Boolean {
        val context = when (invocationEvent) {
            is ApplicationCommandInvocationEvent -> DiscriminatingContext(invocationEvent)
            is ChatCommandInvocationEvent -> DiscriminatingContext(invocationEvent)
            is MessageCommandInvocationEvent -> DiscriminatingContext(invocationEvent)
            is SlashCommandInvocationEvent -> DiscriminatingContext(invocationEvent)
            is UserCommandInvocationEvent -> DiscriminatingContext(invocationEvent)
        }

        return checkCommandRatelimit(invocationEvent.command, context)
    }
}
