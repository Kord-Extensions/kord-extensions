/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.usagelimits.cooldowns

import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.usagelimits.CachedUsageLimitType
import com.kotlindiscord.kord.extensions.usagelimits.DiscriminatingContext
import com.kotlindiscord.kord.extensions.usagelimits.ratelimits.RateLimitType
import com.kotlindiscord.kord.extensions.usagelimits.ratelimits.UsageHistory
import dev.kord.common.DiscordTimestampStyle
import dev.kord.common.toMessageFormat
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.event.interaction.ApplicationCommandInteractionCreateEvent
import dev.kord.core.event.message.MessageCreateEvent
import kotlinx.datetime.Instant
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/** Default [CooldownHandler] implementation, serves as a usable example, it is however very opinionated, so you might
 * want to create your own implementation. **/
public open class DefaultCooldownHandler : CooldownHandler {

    /** Holds the message back-off duration, if the user triggered a cooldown within [backOffTime] ago and now,
     * no message will be sent as the user is considered spamming and wasting our discord api uses. **/
    public open var backOffTime: Duration = 10.seconds

    // cooldown settings provider, collects configured settings for cooldowns :)
    private val cooldownProvider = DefaultCooldownProvider()

    /**
     * Checks if the command should not be run due to a cooldown.
     * If it is on cooldown it saves the cooldown hit and responds with an info message if there was no cooldown hit
     * in the last [backOffTime].
     *
     * Mutates the associated [UsageHistory] of various [UsageLimitTypes][CachedUsageLimitType]
     *
     * @return true if the command is on cooldown, false if not on cooldown.
     */
    override suspend fun checkCommandOnCooldown(context: DiscriminatingContext): Boolean {
        val hitCooldowns = ArrayList<Triple<CooldownType, UsageHistory, Long>>()
        val currentTime = System.currentTimeMillis()
        val encapsulateStart = currentTime - backOffTime.inWholeMilliseconds
        var shouldSendMessage = true

        for (type in CachedUsageLimitType.values()) {
            val until = type.getCooldown(context)
            val usageHistory = type.getUsageHistory(context)

            // keeps only the crossedCooldowns which are in the cooldowns range.
            var i = 0
            while (i < usageHistory.crossedCooldowns.size && usageHistory.crossedCooldowns[i] < encapsulateStart) {
                usageHistory.crossedCooldowns.removeAt(i++)
            }

            if (until > currentTime) {
                if (!shouldSendMessage(until, usageHistory, type)) shouldSendMessage = false
                usageHistory.crossedCooldowns.add(currentTime)

                hitCooldowns.add(Triple(type, usageHistory, until))
            }

            type.setUsageHistory(context, usageHistory)
        }

        if (shouldSendMessage) {
            val (maxType, maxUsageHistory, maxUntil) = hitCooldowns.maxByOrNull {
                it.third
            } ?: return false
            sendCooldownMessage(context, maxType, maxUsageHistory, maxUntil)
        }

        return hitCooldowns.isNotEmpty()
    }

    /**
     * @return true if an "on cooldown" message should be sent, false otherwise.
     */
    override suspend fun shouldSendMessage(
        cooldownUntil: Long,
        usageHistory: UsageHistory,
        type: RateLimitType,
    ): Boolean =
        @Suppress("UnnecessaryParentheses")
        (usageHistory.crossedCooldowns.lastOrNull() ?: 0) < System.currentTimeMillis() - backOffTime.inWholeMilliseconds

    /**
     * Sends a message in the discord channel where the command was used with information about what cooldown
     * was hit and when the user can use the/a command again.
     *
     * The message wil be ephemeral for application commands.
     *
     * @param context the [DiscriminatingContext] that caused this ratelimit hit
     * @param usageHistory the involved [UsageHistory]
     * @param cooldownUntil the involved [epochMillis][Long] timestamp which indicated when the cooldown will end
     */
    override suspend fun sendCooldownMessage(
        context: DiscriminatingContext,
        type: CooldownType,
        usageHistory: UsageHistory,
        cooldownUntil: Long,
    ) {
        val discordTimeStamp = Instant.fromEpochMilliseconds(cooldownUntil)
            .toMessageFormat(DiscordTimestampStyle.RelativeTime)

        val message = getMessage(context, discordTimeStamp, type)

        when (val discordEvent = context.event.event) {
            is MessageCreateEvent -> discordEvent.message.channel.createMessage(message)
            is ApplicationCommandInteractionCreateEvent -> discordEvent.interaction.respondEphemeral {
                content = message
            }
        }
    }

    private suspend fun getMessage(
        context: DiscriminatingContext,
        discordTimeStamp: String,
        type: CooldownType,
    ): String {
        val locale = context.locale()
        val translationsProvider = context.event.command.translationsProvider
        val commandName = context.event.command.getFullName(locale)
        return when (type) {
            CachedUsageLimitType.COMMAND_USER -> translationsProvider.translate(
                "cooldown.notifier.commandUser",
                locale,
                replacements = arrayOf(discordTimeStamp, commandName)
            )

            CachedUsageLimitType.COMMAND_USER_CHANNEL -> translationsProvider.translate(
                "cooldown.notifier.commandUserChannel",
                locale,
                replacements = arrayOf(discordTimeStamp, commandName, context.channel.mention)
            )

            CachedUsageLimitType.COMMAND_USER_GUILD -> translationsProvider.translate(
                "cooldown.notifier.commandUserGuild",
                locale,
                replacements = arrayOf(discordTimeStamp, commandName)
            )

            CachedUsageLimitType.GLOBAL_USER -> translationsProvider.translate(
                "cooldown.notifier.globalUser",
                locale,
                replacements = arrayOf(discordTimeStamp)
            )

            CachedUsageLimitType.GLOBAL_USER_CHANNEL -> translationsProvider.translate(
                "cooldown.notifier.globalUserChannel",
                locale,
                replacements = arrayOf(discordTimeStamp, context.channel.mention)
            )

            CachedUsageLimitType.GLOBAL_USER_GUILD -> translationsProvider.translate(
                "cooldown.notifier.globalUserGuild",
                locale,
                replacements = arrayOf(discordTimeStamp)
            )

            CachedUsageLimitType.GLOBAL -> translationsProvider.translate(
                "cooldown.notifier.global",
                locale,
                replacements = arrayOf(discordTimeStamp)
            )

            CachedUsageLimitType.GLOBAL_CHANNEL -> translationsProvider.translate(
                "cooldown.notifier.globalChannel",
                locale,
                replacements = arrayOf(discordTimeStamp, commandName)
            )

            CachedUsageLimitType.GLOBAL_GUILD -> translationsProvider.translate(
                "cooldown.notifier.globalGuild",
                locale,
                replacements = arrayOf(discordTimeStamp)
            )

            else -> translationsProvider.translate(
                "cooldown.notifier.generic",
                locale,
                replacements = arrayOf(type.toString().lowercase())
            )
        }
    }

    override suspend fun onExecCooldownUpdate(
        commandContext: CommandContext,
        context: DiscriminatingContext,
        success: Boolean,
    ) {
        if (!success) return
        for (t in CachedUsageLimitType.values()) {
            val u = commandContext.command.cooldownMap[t]
            val commandDuration = u?.let { it(context) } ?: Duration.ZERO
            val providedCooldown = cooldownProvider.getCooldown(context, t)
            val progressiveCommandDuration = commandContext.cooldownCounters[t] ?: Duration.ZERO

            val cooldowns = arrayOf(commandDuration, providedCooldown, progressiveCommandDuration)
            val longestDuration = cooldowns.max()

            t.setCooldown(context, System.currentTimeMillis() + longestDuration.inWholeMilliseconds)
        }
    }
}
