package com.kotlindiscord.kord.extensions.usagelimits.ratelimits

import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.usagelimits.CachedUsageLimitType
import com.kotlindiscord.kord.extensions.usagelimits.DiscriminatingContext
import dev.kord.common.DiscordTimestampStyle
import dev.kord.common.toMessageFormat
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.event.interaction.ApplicationCommandInteractionCreateEvent
import dev.kord.core.event.message.MessageCreateEvent
import kotlinx.datetime.Instant
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/** Default [RateLimiter] implementation, serves as a usable example, it is however very opinionated, so you might
 * want to create your own implementation. **/
public open class DefaultRateLimiter : RateLimiter {

    private var rateLimitProvider: RateLimitProvider = DefaultRateLimitProvider()

    /** Holds the message back-off duration, if the user triggered a ratelimit within [backOffTime] ago and now,
     * no message will be sent as the user is considered spamming and wasting our discord api uses. **/
    public open var backOffTime: Duration = 10.seconds

    /**
     * Checks if the command should not be run due to a rateLimit.
     * If so it saves the ratelimit hit and responds only if it is rate-limited.
     *
     * Mutates the associated [UsageHistory] of various [UsageLimitTypes][CachedUsageLimitType]
     *
     * @return true if the command is rate-limited, false if not rate-limited.
     */
    public override suspend fun checkCommandRatelimit(context: DiscriminatingContext): Boolean {
        val currentTimeMillis = System.currentTimeMillis()
        val numerOfTypes = CachedUsageLimitType.values().size
        val hitRateLimits = ArrayList<Triple<RateLimitType, UsageHistory, RateLimit>>(numerOfTypes)
        var shouldSendMessage = true

        for (type in CachedUsageLimitType.values()) {
            val rateLimit = rateLimitProvider.getRateLimit(context, type)
            if (!rateLimit.enabled) continue

            val usageHistory = type.getUsageHistory(context)
            val encapsulateStart = currentTimeMillis - rateLimit.duration.inWholeMilliseconds
            var i = 0

            // keeps only the usageHistory which is in the rateLimit's range,
            // usage that doesn't affect the ratelimit is thus discarded.
            while (i < usageHistory.usages.size && usageHistory.usages[i] < encapsulateStart) {
                usageHistory.usages.removeAt(i++)
            }
            i = 0

            // keeps only the crossedLimits which are in the rateLimit's range.
            while (i < usageHistory.crossedLimits.size && usageHistory.crossedLimits[i] < encapsulateStart) {
                usageHistory.crossedLimits.removeAt(i++)
            }

            if (usageHistory.usages.size > rateLimit.limit) {
                hitRateLimits.add(Triple(type, usageHistory, rateLimit))
                if (!shouldSendMessage(usageHistory, rateLimit, type)) shouldSendMessage = false
                usageHistory.crossedLimits.add(currentTimeMillis)
            }

            type.setUsageHistory(context, usageHistory)
        }

        if (shouldSendMessage) {
            val (maxType, maxUsageHistory, maxRateLimit) = hitRateLimits.maxByOrNull { (_, usageHistory, rateLimit) ->
                rateLimit.duration.minus(usageHistory.usages.first().milliseconds)
            } ?: return false
            sendRateLimitedMessage(context, maxType, maxUsageHistory, maxRateLimit)
        }

        return hitRateLimits.isNotEmpty()
    }

    /**
     * @return true if a ratelimit message should be sent, false otherwise.
     */
    public override suspend fun shouldSendMessage(
        usageHistory: UsageHistory,
        rateLimit: RateLimit,
        type: RateLimitType,
    ): Boolean =
        System.currentTimeMillis() - (usageHistory.crossedLimits.lastOrNull() ?: 0) > backOffTime.inWholeMilliseconds

    /**
     * Sends a message in the discord channel where the command was used with information about what ratelimit
     * was hit and when the user can use the/a command again.
     *
     * The message wil be ephemeral for application commands.
     *
     * @param context the [CommandContext] that caused this ratelimit hit
     * @param usageHistory the involved [UsageHistory]
     * @param rateLimit the involved [RateLimit]
     */
    public override suspend fun sendRateLimitedMessage(
        context: DiscriminatingContext,
        type: RateLimitType,
        usageHistory: UsageHistory,
        rateLimit: RateLimit,
    ) {
        val restOfRateLimitDuration = rateLimit.duration.minus(usageHistory.usages.first().milliseconds)
        val endOfRateLimit = System.currentTimeMillis() + restOfRateLimitDuration.inWholeMilliseconds
        val discordTimeStamp =
            Instant.fromEpochMilliseconds(endOfRateLimit).toMessageFormat(DiscordTimestampStyle.RelativeTime)
        val message =
            "You are being RateLimited until $discordTimeStamp, please stop spamming.\n" +
                " ${rateLimit.limit} actions per ${rateLimit.duration.toIsoString()} is the limit."

        when (val discordEvent = context.event.event) {
            is MessageCreateEvent -> discordEvent.message.channel.createMessage(message)
            is ApplicationCommandInteractionCreateEvent -> discordEvent.interaction.respondEphemeral {
                content = message
            }
        }
    }
}
