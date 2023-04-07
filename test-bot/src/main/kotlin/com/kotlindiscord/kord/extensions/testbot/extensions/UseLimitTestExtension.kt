/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.testbot.extensions

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.publicSubCommand
import com.kotlindiscord.kord.extensions.commands.converters.impl.duration
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import com.kotlindiscord.kord.extensions.usagelimits.CachedCommandLimitTypes
import com.kotlindiscord.kord.extensions.utils.toDuration
import kotlinx.datetime.DateTimePeriod
import kotlinx.datetime.TimeZone
import kotlin.time.Duration.Companion.seconds

public class UseLimitTestExtension : Extension() {
    override val name: String = "use-limit"

    override suspend fun setup() {
        publicSlashCommand {
            name = "cooldown"
            description = "cooldown group"

            publicSubCommand {
                name = "ten"
                description = "Slash command with a 10 second cooldown"

                // Set cooldown which is applied on each command execution for this command
                cooldown(CachedCommandLimitTypes.CommandUser) { 10.seconds }

                action {
                    respond {
                        content = "Not on cooldown \uD83D\uDC1F" // fish emoji
                    }
                }
            }

            publicSubCommand(::CustomCDArguments) {
                name = "custom"
                description = "Slash command with a custom cooldown"

                action {
                    val duration = arguments.cooldown.toDuration(TimeZone.UTC)

                    // Increment cooldowns at command runtime
                    cooldowns.inc(CachedCommandLimitTypes.CommandUser, duration)

                    respond {
                        content = "Not on cooldown \uD83D\uDC1F" // fish emoji
                    }
                }
            }
        }
    }

    public class CustomCDArguments : Arguments() {
        public val cooldown: DateTimePeriod by duration {
            name = "duration"
            description = "Duration for the cooldown, ex. 10s, 1m, 1h, 1d, 1w, 1M, 1y."
        }
    }
}
