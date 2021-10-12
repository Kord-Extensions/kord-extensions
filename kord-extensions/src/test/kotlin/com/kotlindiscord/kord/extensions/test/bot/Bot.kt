package com.kotlindiscord.kord.extensions.test.bot

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.checks.isNotBot
import com.kotlindiscord.kord.extensions.utils.env
import org.koin.core.logger.Level

suspend fun main() {
    val bot = ExtensibleBot(env("TOKEN")) {
        koinLogLevel = Level.DEBUG

        chatCommands {
            defaultPrefix = "?"
            enabled = true

            check { isNotBot() }

            prefix { default ->
                if (guildId?.asString == "787452339908116521") {
                    "!"
                } else {
                    default  // "?"
                }
            }
        }

        applicationCommands {
            defaultGuild("787452339908116521")
        }

        intents {
//            +Intent.GuildMessages
        }

        members {
            none()
        }

        extensions {
            add(::TestExtension)

            help {
                paginatorTimeout = 5
            }
        }
    }

    bot.start()
}
