/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.usagelimits.cooldowns

import com.kotlindiscord.kord.extensions.commands.CommandContext
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
 * Default [CooldownHandler] implementation, serves as a usable example, it is however very opinionated, so you might
 * want to create your own implementation.
 */
public open class DefaultCooldownHandler : CooldownHandler {

    /** Cooldown settings provider, collects configured settings for cooldowns. **/
    public open var cooldownProvider: CooldownProvider = DefaultCooldownProvider()

    /**
     * Holds the message back-off duration, if the user triggered a cooldown within [backOffTimeSpan] ago and now,
     * no message will be sent.
     */
    public open var backOffTimeSpan: Duration = 10.seconds

    /**
     * Checks if the command should not be run due to a cooldown.
     * Sends a message to the user if a cooldown is hit and [shouldSendMessage] returns true.
     *
     * Mutates the associated [CooldownHistory] of [previously used][DefaultCooldownProvider.getCooldownTypes]
     * [cooldownTypes][CooldownType] to reflect the current system state.
     *
     * @return true if the command is on cooldown, false otherwise.
     */
    override suspend fun checkCommandOnCooldown(context: DiscriminatingContext): Boolean {
        val cooldownTypes = cooldownProvider.getCooldownTypes(null, context)
        val hitCooldowns = ArrayList<Triple<CooldownType, CooldownHistory, Instant>>()
        val currentTime = Clock.System.now()
        val encapsulateStart = currentTime - backOffTimeSpan

        var shouldSendMessage = true

        for (type in cooldownTypes) {
            val until = type.getCooldown(context)
            val cooldownHistory = type.getCooldownUsageHistory(context)

            // keeps only the crossedCooldowns which are in the cooldowns range.
            cooldownHistory.removeExpiredCooldownHits(encapsulateStart)

            if (until > currentTime) {
                if (!shouldSendMessage(until, cooldownHistory, type)) {
                    shouldSendMessage = false
                }

                cooldownHistory.addCooldownHit(currentTime)
                hitCooldowns.add(Triple(type, cooldownHistory, until))
            }

            type.setCooldownUsageHistory(context, cooldownHistory)
        }

        if (shouldSendMessage) {
            val (
                maxType, maxUsageHistory, maxUntil,
            ) = hitCooldowns.maxByOrNull { it.third } ?: return false

            sendCooldownMessage(context, maxType, maxUsageHistory, maxUntil)
        }

        return hitCooldowns.isNotEmpty()
    }

    /** @return true if there was no cooldown hit in the last [backOffTimeSpan], false otherwise. **/
    @Suppress("UnnecessaryParentheses")
    override suspend fun shouldSendMessage(
        cooldownUntil: Instant,
        usageHistory: CooldownHistory,
        type: CooldownType,
    ): Boolean =
        (usageHistory.crossedCooldowns.lastOrNull() ?: Instant.DISTANT_PAST) < Clock.System.now() - backOffTimeSpan

    /**
     * Sends a cooldown message in the discord channel where the command was used.
     *
     * The message wil be ephemeral for application commands.
     *
     * @param context the [DiscriminatingContext] that caused this cooldown
     * @param type the [CooldownType] that was hit
     * @param usageHistory the current [CooldownHistory] for this [type]
     * @param cooldownUntil when the cooldown will be over
     */
    override suspend fun sendCooldownMessage(
        context: DiscriminatingContext,
        type: CooldownType,
        usageHistory: CooldownHistory,
        cooldownUntil: Instant,
    ) {
        val message = getMessage(context, cooldownUntil, type)

        context.event.event.sendEphemeralMessage(message)
    }

    /** @return Message about what cooldown has been hit. **/
    public open suspend fun getMessage(
        context: DiscriminatingContext,
        cooldownUntil: Instant,
        type: CooldownType,
    ): String {
        val locale = context.locale()
        val translationsProvider = context.event.command.translationsProvider
        val commandName = context.event.command.getFullName(locale)
        val discordTimeStamp = cooldownUntil.toMessageFormat(DiscordTimestampStyle.RelativeTime)

        return when (type) {
            CachedCommandLimitTypes.CommandUser -> translationsProvider.translate(
                "cooldown.notifier.commandUser",
                locale,
                replacements = arrayOf(discordTimeStamp, commandName)
            )

            CachedCommandLimitTypes.CommandUserChannel -> translationsProvider.translate(
                "cooldown.notifier.commandUserChannel",
                locale,
                replacements = arrayOf(discordTimeStamp, commandName, context.channel.mention)
            )

            CachedCommandLimitTypes.CommandUserGuild -> translationsProvider.translate(
                "cooldown.notifier.commandUserGuild",
                locale,
                replacements = arrayOf(discordTimeStamp, commandName)
            )

            CachedCommandLimitTypes.GlobalUser -> translationsProvider.translate(
                "cooldown.notifier.globalUser",
                locale,
                replacements = arrayOf(discordTimeStamp)
            )

            CachedCommandLimitTypes.GlobalUserChannel -> translationsProvider.translate(
                "cooldown.notifier.globalUserChannel",
                locale,
                replacements = arrayOf(discordTimeStamp, context.channel.mention)
            )

            CachedCommandLimitTypes.GlobalUserGuild -> translationsProvider.translate(
                "cooldown.notifier.globalUserGuild",
                locale,
                replacements = arrayOf(discordTimeStamp)
            )

            CachedCommandLimitTypes.GlobalChannel -> translationsProvider.translate(
                "cooldown.notifier.globalChannel",
                locale,
                replacements = arrayOf(discordTimeStamp, commandName)
            )

            CachedCommandLimitTypes.GlobalGuild -> translationsProvider.translate(
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

    /**
     * Called after a command ran.
     *
     * Stores the longest cooldown for each [previously used][DefaultCooldownProvider.getCooldownTypes] [CooldownType].
     *
     * @param commandContext the [CommandContext] of the command that was executed
     * @param context the [DiscriminatingContext] that caused this cooldown
     * @param success true if the command was executed successfully, false otherwise
     */
    override suspend fun onExecCooldownUpdate(
        commandContext: CommandContext,
        context: DiscriminatingContext,
        success: Boolean,
    ) {
        if (!success) {
            return
        }

        val cooldownTypes = cooldownProvider.getCooldownTypes(commandContext, context)
        for (cooldownType in cooldownTypes) {
            val longestCooldown = cooldownProvider.getCooldown(commandContext, context, cooldownType)

            if (longestCooldown == Duration.ZERO) {
                continue
            }

            cooldownType.setCooldown(context, Clock.System.now() + longestCooldown)
        }
    }
}
