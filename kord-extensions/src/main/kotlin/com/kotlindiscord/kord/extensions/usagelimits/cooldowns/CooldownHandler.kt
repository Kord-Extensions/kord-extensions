/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.usagelimits.cooldowns

import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.events.*
import com.kotlindiscord.kord.extensions.usagelimits.DiscriminatingContext
import kotlinx.datetime.Instant

/**
 * A cooldown handler is responsible for checking if a command is on cooldown and sending a cooldown message.
 * As well as updating the cooldowns after a command execution.
 */
public interface CooldownHandler {

    /**
     * Checks if the command is on cooldown.
     * Called before a command is executed.
     *
     * @return true if the command is on cooldown, false otherwise
     */
    public suspend fun checkCommandOnCooldown(context: DiscriminatingContext): Boolean

    /**
     * @return true if a cooldown message should be sent, false otherwise
     */
    public suspend fun shouldSendMessage(
        cooldownUntil: Instant,
        usageHistory: CooldownHistory,
        type: CooldownType
    ): Boolean

    /**
     * Sends a cooldown message.
     *
     * @param context the [CommandContext] that caused this ratelimit hit
     * @param type the type/scope of the cooldown
     * @param usageHistory the involved [UsageHistory]
     * @param cooldownUntil the moment at which the cooldown ends
     */
    public suspend fun sendCooldownMessage(
        context: DiscriminatingContext,
        type: CooldownType,
        usageHistory: CooldownHistory,
        cooldownUntil: Instant
    )

    /**
     * Checks if the command is on cooldown.
     *
     * @return true if the command is on cooldown, false otherwise
     */
    public suspend fun checkCommandOnCooldown(invocationEvent: CommandInvocationEvent<*, *>): Boolean {
        val context = getContext(invocationEvent)
        return checkCommandOnCooldown(context)
    }

    private fun getContext(invocationEvent: CommandInvocationEvent<*, *>): DiscriminatingContext {
        val context = when (invocationEvent) {
            is ApplicationCommandInvocationEvent -> DiscriminatingContext(invocationEvent)
            is ChatCommandInvocationEvent -> DiscriminatingContext(invocationEvent)
            is MessageCommandInvocationEvent -> DiscriminatingContext(invocationEvent)
            is SlashCommandInvocationEvent -> DiscriminatingContext(invocationEvent)
            is UserCommandInvocationEvent -> DiscriminatingContext(invocationEvent)
        }
        return context
    }

    /**
     * Called after a command ran.
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
     * Called after a command ran.
     *
     * @param commandContext the [CommandContext] of the command that was executed
     * @param context the [DiscriminatingContext] that caused this cooldown
     * @param success true if the command was executed successfully, false otherwise
     */
    public suspend fun onExecCooldownUpdate(
        commandContext: CommandContext,
        context: DiscriminatingContext,
        success: Boolean
    )
}
