package com.kotlindiscord.kord.extensions.test.bot

import com.kotlindiscord.kord.extensions.ExtensibleBot
import org.koin.core.logger.Level

suspend fun main() {
    val bot = ExtensibleBot(System.getenv("TOKEN")) {
        koinLogLevel = Level.DEBUG

        commands {
            defaultPrefix = "?"
            slashCommands = true

            prefix { default ->
                if (guildId?.asString == "787452339908116521") {
                    "!"
                } else {
                    default  // "?"
                }
            }
        }

        extensions {
            add(::TestExtension)
        }
    }

    bot.start()
}
