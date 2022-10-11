/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.usagelimits.cooldowns

import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.events.ApplicationCommandInvocationEvent
import com.kotlindiscord.kord.extensions.commands.events.ChatCommandInvocationEvent
import com.kotlindiscord.kord.extensions.commands.events.CommandInvocationEvent
import com.kotlindiscord.kord.extensions.usagelimits.DiscriminatingContext
import com.kotlindiscord.kord.extensions.usagelimits.UsageLimitType
import com.kotlindiscord.kord.extensions.usagelimits.ratelimits.RateLimitType
import com.kotlindiscord.kord.extensions.usagelimits.ratelimits.UsageHistory

/**
 * Abstraction that allows you to implement custom cooldown behaviour.
 * **/
public interface CooldownHandler {

    /**
     * Checks if the command should not be run due to a cooldown.
     * If so it should save the cooldown hit and can give a response only if it is on cooldown.
     *
     * Mutates the associated [UsageHistory] of various [UsageLimitTypes][UsageLimitType]
     *
     * @return true if the command is on cooldown, false if not on cooldown
     */
    public suspend fun checkCommandOnCooldown(context: DiscriminatingContext): Boolean

    /**
     * @return true if a cooldown message should be sent, false otherwise
     */
    public suspend fun shouldSendMessage(cooldownUntil: Long, usageHistory: UsageHistory, type: RateLimitType): Boolean

    /**
     * Should send a message in the discord channel where the command was used with information about what cooldown
     * was hit and when the user can use the command again.
     * Can be adapted to also log information into a logChannel (not an implementation goal)
     *
     * @param context the [CommandContext] that caused this ratelimit hit
     * @param type the type/scope of the cooldown
     * @param usageHistory the involved [UsageHistory]
     * @param cooldownUntil the epochMillis moment at which the cooldown ends
     */
    public suspend fun sendCooldownMessage(
        context: DiscriminatingContext,
        type: CooldownType,
        usageHistory: UsageHistory,
        cooldownUntil: Long
    )

    /**
     * Wrapper function to convert invocationEvent into the required [DiscriminatingContext]
     * Checks if the command should not be run due to a cooldown.
     * If so it should save the ratelimit hit and can give a response only if it is on cooldown.
     *
     * Mutates the associated [UsageHistory] of various [UsageLimitTypes][UsageLimitType]
     *
     * @return true if the command is on cooldown, false if not on cooldown
     */
    public suspend fun checkCommandOnCooldown(invocationEvent: CommandInvocationEvent<*, *>): Boolean {
        val context = getContext(invocationEvent)
        return checkCommandOnCooldown(context)
    }

    private fun getContext(invocationEvent: CommandInvocationEvent<*, *>): DiscriminatingContext {
        val context = when (invocationEvent) {
            is ApplicationCommandInvocationEvent -> DiscriminatingContext(invocationEvent)
            is ChatCommandInvocationEvent -> DiscriminatingContext(invocationEvent)
        }
        return context
    }

    /**
     * Executed after a command execution. Stores the longest cooldown for each configured [CooldownType].
     */
    public suspend fun onExecCooldownUpdate(
        commandContext: CommandContext,
        invocationEvent: CommandInvocationEvent<*, *>,
        success: Boolean
    ) {
        val context = getContext(invocationEvent)
        return onExecCooldownUpdate(commandContext, context, success)
    }

    /**
     * Executed after a command execution. Stores the longest cooldown for each configured [CooldownType].
     */
    public suspend fun onExecCooldownUpdate(
        commandContext: CommandContext,
        context: DiscriminatingContext,
        success: Boolean
    )
}
