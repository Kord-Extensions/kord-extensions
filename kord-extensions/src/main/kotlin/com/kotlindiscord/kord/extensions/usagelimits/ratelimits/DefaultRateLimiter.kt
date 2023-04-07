/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.usagelimits.ratelimits

import com.kotlindiscord.kord.extensions.commands.Command
import com.kotlindiscord.kord.extensions.usagelimits.CachedCommandLimitTypes
import com.kotlindiscord.kord.extensions.usagelimits.DiscriminatingContext
import com.kotlindiscord.kord.extensions.usagelimits.sendEphemeralMessage
import dev.kord.common.DiscordTimestampStyle
import dev.kord.common.toMessageFormat
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Default [RateLimiter] implementation, serves as a usable example, it is however very opinionated, so you might
 * want to create your own implementation.
 */
public open class DefaultRateLimiter : RateLimiter {

    /** rateLimit settings provider, collects configured settings for rateLimits. **/
    public open var rateLimitProvider: RateLimitProvider = DefaultRateLimitProvider()

    /**
     * Holds the message back-off duration, if the user triggered a ratelimit within [backOffTime] ago and now,
     * no message will be sent as the user is considered spamming and wasting our discord api uses.
     */
    public open var backOffTime: Duration = 10.seconds

    /**
     * Checks whether the command should be run.
     * This is called before the command is executed.
     *
     * Send a message if the user is rateLimited and the last message was sent more than [backOffTime] ago.
     *
     * Mutates the associated [RateLimitHistory] of [previously used][DefaultRateLimitProvider.getRateLimitTypes]
     * [rateLimitTypes][RateLimitType] to reflect the current system state.
     * (e.g. a command is (attempted to be/going to be) executed)
     *
     * @param command the [Command] that is being executed
     * @param context the [DiscriminatingContext] that caused this ratelimit hit
     *
     * @return true if the command is rate-limited, false if not rate-limited.
     */
    public override suspend fun checkCommandRatelimit(command: Command, context: DiscriminatingContext): Boolean {
        val currentTime = Clock.System.now()
        val rateLimitTypes = rateLimitProvider.getRateLimitTypes(null, context)
        val hitRateLimits = ArrayList<Triple<RateLimitType, RateLimitHistory, RateLimit>>(rateLimitTypes.size)
        var shouldSendMessage = true

        for (type in rateLimitTypes) {
            val rateLimit = rateLimitProvider.getRateLimit(command, context, type)
            if (!rateLimit.enabled) {
                continue
            }

            val rateLimitHistory = type.getRateLimitUsageHistory(context)
            val encapsulateStart = currentTime - rateLimit.duration

            // keeps only the usageHistory which is in the rateLimit's range,
            // usage that doesn't affect the ratelimit is thus discarded.
            rateLimitHistory.removeExpiredUsages(encapsulateStart)

            // keeps only the crossedLimits which are in the rateLimit's range.
            rateLimitHistory.removeExpiredRateLimitHits(encapsulateStart)

            if (rateLimitHistory.usages.size > rateLimit.limit) {
                hitRateLimits.add(Triple(type, rateLimitHistory, rateLimit))

                if (!shouldSendMessage(rateLimitHistory, rateLimit, type)) {
                    shouldSendMessage = false
                }

                rateLimitHistory.addRateLimitHit(currentTime)
            } else {
                rateLimitHistory.addUsage(currentTime)
            }

            type.setRateLimitUsageHistory(context, rateLimitHistory)
        }

        if (shouldSendMessage) {
            val (
                maxType, maxUsageHistory, maxRateLimit
            ) = hitRateLimits.maxByOrNull { (_, usageHistory, rateLimit) ->
                // Computes the duration until the ratelimit is lifted due to the first usage going out of range.
                rateLimit.duration - (currentTime - usageHistory.usages.first())
            } ?: return false

            sendRateLimitedMessage(context, maxType, maxUsageHistory, maxRateLimit)
        }

        return hitRateLimits.isNotEmpty()
    }

    /** @return true if a ratelimit message should be sent, false otherwise. **/
    public override suspend fun shouldSendMessage(
        usageHistory: RateLimitHistory,
        rateLimit: RateLimit,
        type: RateLimitType,
    ): Boolean = Clock.System.now() - (usageHistory.rateLimitHits.lastOrNull() ?: Instant.DISTANT_PAST) > backOffTime

    /** @return Message about what ratelimit has been hit. **/
    public open suspend fun getMessage(
        context: DiscriminatingContext,
        endOfRateLimit: Instant,
        type: RateLimitType,
    ): String {
        val locale = context.locale()
        val translationsProvider = context.event.command.translationsProvider
        val commandName = context.event.command.getFullName(locale)
        val discordTimeStamp = endOfRateLimit.toMessageFormat(DiscordTimestampStyle.RelativeTime)

        return when (type) {
            CachedCommandLimitTypes.CommandUser -> translationsProvider.translate(
                "ratelimit.notifier.commandUser",
                locale,
                replacements = arrayOf(discordTimeStamp, commandName)
            )

            CachedCommandLimitTypes.CommandUserChannel -> translationsProvider.translate(
                "ratelimit.notifier.commandUserChannel",
                locale,
                replacements = arrayOf(discordTimeStamp, commandName, context.channel.mention)
            )

            CachedCommandLimitTypes.CommandUserGuild -> translationsProvider.translate(
                "ratelimit.notifier.commandUserGuild",
                locale,
                replacements = arrayOf(discordTimeStamp, commandName)
            )

            CachedCommandLimitTypes.GlobalUser -> translationsProvider.translate(
                "ratelimit.notifier.globalUser",
                locale,
                replacements = arrayOf(discordTimeStamp)
            )

            CachedCommandLimitTypes.GlobalUserChannel -> translationsProvider.translate(
                "ratelimit.notifier.globalUserChannel",
                locale,
                replacements = arrayOf(discordTimeStamp, context.channel.mention)
            )

            CachedCommandLimitTypes.GlobalUserGuild -> translationsProvider.translate(
                "ratelimit.notifier.globalUserGuild",
                locale,
                replacements = arrayOf(discordTimeStamp)
            )

            CachedCommandLimitTypes.GlobalChannel -> translationsProvider.translate(
                "ratelimit.notifier.globalChannel",
                locale,
                replacements = arrayOf(discordTimeStamp, context.channel.mention)
            )

            CachedCommandLimitTypes.GlobalGuild -> translationsProvider.translate(
                "ratelimit.notifier.globalGuild",
                locale,
                replacements = arrayOf(discordTimeStamp)
            )

            else -> translationsProvider.translate(
                "ratelimit.notifier.generic",
                locale,
                replacements = arrayOf(type.toString().lowercase())
            )
        }
    }

    /**
     * Sends a message in the discord channel where the command was used.
     *
     * The message wil be ephemeral for application commands.
     *
     * @param context the [DiscriminatingContext] that caused this ratelimit hit
     * @param type the [RateLimitType] that was hit
     * @param rateLimitHistory the current [RateLimitHistory] for this [type]
     * @param rateLimit the involved [RateLimit]
     */
    public override suspend fun sendRateLimitedMessage(
        context: DiscriminatingContext,
        type: RateLimitType,
        rateLimitHistory: RateLimitHistory,
        rateLimit: RateLimit,
    ) {
        val restOfRateLimitDuration = rateLimit.duration - (Clock.System.now() - rateLimitHistory.usages.first())
        val endOfRateLimit = Clock.System.now() + restOfRateLimitDuration
        val message = getMessage(context, endOfRateLimit, type)

        context.event.event.sendEphemeralMessage(message)
    }
}
