/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.usagelimits.ratelimits

import com.kotlindiscord.kord.extensions.builders.ExtensibleBotBuilder
import com.kotlindiscord.kord.extensions.commands.Command
import com.kotlindiscord.kord.extensions.commands.events.ChatCommandInvocationEvent
import com.kotlindiscord.kord.extensions.commands.events.SlashCommandInvocationEvent
import com.kotlindiscord.kord.extensions.usagelimits.DiscriminatingContext
import com.kotlindiscord.kord.extensions.utils.getKoin

/**
 * Default [RateLimitProvider] implementation.
 *
 * Provides ratelimit info from commands and global settings.
 */
public class DefaultRateLimitProvider : RateLimitProvider {

    private val settings: ExtensibleBotBuilder by lazy { getKoin().get() }

    // This is used for collecting all types that were used during runtime and types that were configured.
    private val usedRateLimitTypes = mutableSetOf<RateLimitType>()

    /** @return rateLimit types from the contexts and global settings. **/
    override suspend fun getRateLimitTypes(
        command: Command?,
        context: DiscriminatingContext,
    ): Set<RateLimitType> {
        val typesFromContext =
            command?.ratelimits?.keys.orEmpty() +
                settings.applicationCommandsBuilder.useLimiterBuilder.rateLimits.keys +
                settings.chatCommandsBuilder.useLimiterBuilder.rateLimits.keys

        usedRateLimitTypes.addAll(typesFromContext)
        return usedRateLimitTypes
    }

    /**
     * @return The [rateLimit][RateLimit] that is defined at the lowest level, based on the contexts and [type].
     *
     * levels (smaller is lower): [command] ratelimits <
     * [global][ExtensibleBotBuilder.UseLimiterBuilder.ratelimit] ratelimits
     */
    override suspend fun getRateLimit(
        command: Command,
        context: DiscriminatingContext,
        type: RateLimitType,
    ): RateLimit {
        val commandRateLimit = command.ratelimits[type]?.invoke(context)
        if (commandRateLimit != null) {
            return commandRateLimit
        }

        val globalRateLimit = when (context.event) {
            is SlashCommandInvocationEvent ->
                settings.applicationCommandsBuilder.useLimiterBuilder.rateLimits[type]?.invoke(context)
                    ?: RateLimit.disabled()

            is ChatCommandInvocationEvent ->
                settings.chatCommandsBuilder.useLimiterBuilder.rateLimits[type]?.invoke(context) ?: RateLimit.disabled()

            else -> RateLimit.disabled()
        }

        return globalRateLimit
    }
}
