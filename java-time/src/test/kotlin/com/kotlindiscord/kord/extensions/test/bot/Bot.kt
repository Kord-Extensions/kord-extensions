package com.kotlindiscord.kord.extensions.test.bot

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.utils.env
import org.koin.core.logger.Level
import java.util.*

suspend fun main() {
    val bot = ExtensibleBot(env("TOKEN")!!) {
        koinLogLevel = Level.DEBUG

        i18n {
            localeResolver { guild, channel, user ->
                when(user?.id?.value) {
                    242043299022635020 -> Locale.FRANCE
                    667552017434017794 -> Locale.SIMPLIFIED_CHINESE
                    185461862878543872 -> Locale.GERMAN

                    else -> defaultLocale
                }
            }
        }

        messageCommands {
            defaultPrefix = "?"

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
