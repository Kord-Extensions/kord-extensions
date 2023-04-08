/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.testbot

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.checks.isNotBot
import com.kotlindiscord.kord.extensions.modules.extra.phishing.extPhishing
import com.kotlindiscord.kord.extensions.modules.extra.pluralkit.extPluralKit
import com.kotlindiscord.kord.extensions.testbot.extensions.*
import com.kotlindiscord.kord.extensions.testbot.extensions.PaginatorTestExtension
import com.kotlindiscord.kord.extensions.testbot.utils.LogLevel
import com.kotlindiscord.kord.extensions.usagelimits.CachedCommandLimitTypes
import com.kotlindiscord.kord.extensions.usagelimits.cooldowns.DefaultCooldownHandler
import com.kotlindiscord.kord.extensions.usagelimits.ratelimits.DefaultRateLimiter
import com.kotlindiscord.kord.extensions.usagelimits.ratelimits.RateLimit
import com.kotlindiscord.kord.extensions.utils.env
import com.kotlindiscord.kord.extensions.utils.envOrNull
import dev.kord.common.Locale
import dev.kord.common.entity.Snowflake
import dev.kord.gateway.Intents
import dev.kord.gateway.PrivilegedIntent
import org.koin.core.logger.Level
import kotlin.time.Duration.Companion.seconds

public val TEST_SERVER_ID: Snowflake = Snowflake(env("TEST_SERVER"))

@OptIn(PrivilegedIntent::class)
public suspend fun main() {
    LogLevel.enabledLevel = LogLevel.fromString(envOrNull("LOG_LEVEL") ?: "INFO") ?: LogLevel.INFO

    val bot = ExtensibleBot(env("TOKEN")) {
        koinLogLevel = Level.DEBUG

        chatCommands {
            enabled = true

            check { isNotBot() }
        }

        applicationCommands {
            defaultGuild(TEST_SERVER_ID)

            useLimiter {
                // NOTE: You might want to use the same instances for these between chatCommands and
                // application commands.
                // If you use separate instances, the limits will not be shared between the two command types.
                cooldownHandler = DefaultCooldownHandler()
                rateLimiter = DefaultRateLimiter()

                // Example cooldown, users can only run  command every 5 seconds, per-server
                cooldown(CachedCommandLimitTypes.CommandUserGuild) { 5.seconds }

                // Example ratelimit, there can only be 20 commands ran in a channel during the last 60 seconds.
                ratelimit(CachedCommandLimitTypes.GlobalChannel) { RateLimit(true, 20, 60.seconds) }
            }
        }

        intents {
            +Intents.all
        }

        i18n {
            interactionUserLocaleResolver()

            applicationCommandLocale(Locale.CHINESE_CHINA)
            applicationCommandLocale(Locale.ENGLISH_GREAT_BRITAIN)
            applicationCommandLocale(Locale.ENGLISH_UNITED_STATES)
            applicationCommandLocale(Locale.GERMAN)
            applicationCommandLocale(Locale.JAPANESE)
        }

        members {
            all()
        }

        extensions {
            help {
                paginatorTimeout = 30
            }

            extPhishing {
                appName = "Integration test bot"
                logChannelName = "alerts"
            }

            if (envOrNull("PLURALKIT_TESTING") != null) {
                extPluralKit()
            }

            add(::ArgumentTestExtension)
            add(::I18nTestExtension)
            add(::ModalTestExtension)
            add(::PaginatorTestExtension)
            add(::UseLimitTestExtension)
            add(::PKTestExtension)
        }

        plugins {
            pluginPaths.clear()

            pluginPath("test-bot/build/generated/ksp/main/resources")
            pluginPath("extra-modules/extra-mappings/build/generated/ksp/main/resources")
        }
    }

    bot.start()
}
