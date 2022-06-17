/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.testbot

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.checks.isNotBot
import com.kotlindiscord.kord.extensions.testbot.extensions.I18nTestExtension
import com.kotlindiscord.kord.extensions.testbot.utils.LogLevel
import com.kotlindiscord.kord.extensions.utils.env
import com.kotlindiscord.kord.extensions.utils.envOrNull
import dev.kord.common.Locale
import dev.kord.common.entity.Snowflake
import dev.kord.gateway.Intents
import dev.kord.gateway.PrivilegedIntent
import org.koin.core.logger.Level

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
        }

        intents {
            +Intents.all
        }

        i18n {
            interactionUserLocaleResolver()

            applicationCommandLocale(Locale.ENGLISH_GREAT_BRITAIN)
            applicationCommandLocale(Locale.ENGLISH_UNITED_STATES)
            applicationCommandLocale(Locale.JAPANESE)
        }

        members {
            all()
        }

        extensions {
            help {
                paginatorTimeout = 30
            }

            add(::I18nTestExtension)
        }

        plugins {
            pluginPaths.clear()

            pluginPath("test-bot/build/generated/ksp/main/resources")
            pluginPath("extra-modules/extra-mappings/build/generated/ksp/main/resources")
        }
    }

    bot.start()
}
