package com.kotlindiscord.kord.extensions.test.bot

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.checks.isNotBot
import com.kotlindiscord.kord.extensions.modules.extra.phishing.extPhishing
import com.kotlindiscord.kord.extensions.utils.env
import org.koin.core.logger.Level

suspend fun main() {
    val bot = ExtensibleBot(env("TOKEN")) {
        koinLogLevel = Level.DEBUG

        chatCommands {
            check { isNotBot() }
            enabled = true
        }

        applicationCommands {
            enabled = true
        }

        extensions {
            extPhishing {
                appName = "Integration test bot"
                logChannelName = "alerts"
            }
        }
    }

    bot.start()
}
