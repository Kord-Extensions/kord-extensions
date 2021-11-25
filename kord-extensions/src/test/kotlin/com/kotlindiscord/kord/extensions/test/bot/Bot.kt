package com.kotlindiscord.kord.extensions.test.bot

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.checks.isNotBot
import com.kotlindiscord.kord.extensions.utils.env
import dev.kord.common.entity.Snowflake
import org.koin.core.logger.Level

val TEST_SERVER_ID = Snowflake(787452339908116521UL)

suspend fun main() {
    val bot = ExtensibleBot(env("TOKEN")) {
        koinLogLevel = Level.DEBUG

        chatCommands {
            defaultPrefix = "?"
            enabled = true

            check { isNotBot() }

            prefix { default ->
                if (guildId == TEST_SERVER_ID) {
                    "!"
                } else {
                    default  // "?"
                }
            }
        }

        applicationCommands {
            defaultGuild(TEST_SERVER_ID)
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
