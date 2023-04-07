/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.usagelimits.cooldowns

import com.kotlindiscord.kord.extensions.builders.ExtensibleBotBuilder
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.events.ChatCommandInvocationEvent
import com.kotlindiscord.kord.extensions.commands.events.SlashCommandInvocationEvent
import com.kotlindiscord.kord.extensions.usagelimits.DiscriminatingContext
import com.kotlindiscord.kord.extensions.utils.getKoin
import kotlin.time.Duration

/**
 * Default [CooldownProvider] implementation.
 *
 * Provides cooldown info from commands, global settings, from executed commands' contexts.
 */
public class DefaultCooldownProvider : CooldownProvider {

    private val settings: ExtensibleBotBuilder by lazy { getKoin().get() }

    // This is used for collecting all types that were used during runtime and types that were configured.
    private val usedCooldownTypes = mutableSetOf<CooldownType>()

    /** @return Union of the previous used-, context- and global setting cooldown types.  **/
    public override suspend fun getCooldownTypes(
        commandContext: CommandContext?,
        context: DiscriminatingContext,
    ): Set<CooldownType> {
        val typesFromContext =
            commandContext?.command?.cooldowns?.keys.orEmpty() + commandContext?.cooldowns?.keys.orEmpty() +
                settings.applicationCommandsBuilder.useLimiterBuilder.cooldowns.keys +
                settings.chatCommandsBuilder.useLimiterBuilder.cooldowns.keys

        usedCooldownTypes.addAll(typesFromContext)
        return usedCooldownTypes
    }

    /** @return The longest cooldown [Duration] based on the contexts and [type]. **/
    public override suspend fun getCooldown(
        commandContext: CommandContext,
        context: DiscriminatingContext,
        type: CooldownType,
    ): Duration {
        val commandDuration = commandContext.command.cooldowns[type]?.invoke(context) ?: Duration.ZERO
        val progressiveCommandDuration = commandContext.cooldowns[type] ?: Duration.ZERO

        val globalCooldown = when (context.event) {
            is SlashCommandInvocationEvent ->
                settings.applicationCommandsBuilder.useLimiterBuilder.cooldowns[type]?.invoke(context)
                    ?: Duration.ZERO

            is ChatCommandInvocationEvent ->
                settings.chatCommandsBuilder.useLimiterBuilder.cooldowns[type]?.invoke(context) ?: Duration.ZERO

            else -> Duration.ZERO
        }

        val cooldowns = arrayOf(commandDuration, globalCooldown, progressiveCommandDuration)
        return cooldowns.max()
    }
}
