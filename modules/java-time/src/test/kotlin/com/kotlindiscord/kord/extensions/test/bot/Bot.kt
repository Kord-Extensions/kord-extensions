package com.kotlindiscord.kord.extensions.test.bot

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.i18n.SupportedLocales
import com.kotlindiscord.kord.extensions.utils.env
import org.koin.core.logger.Level

suspend fun main() {
    val bot = ExtensibleBot(env("TOKEN")) {
        koinLogLevel = Level.DEBUG

        i18n {
            localeResolver { _, _, user ->
                @Suppress("UnderscoresInNumericLiterals")
                when (user?.id?.value) {
                    560515299388948500UL -> SupportedLocales.FINNISH
                    242043299022635020UL -> SupportedLocales.FRENCH
                    407110650217627658UL -> SupportedLocales.FRENCH
                    667552017434017794UL -> SupportedLocales.CHINESE_SIMPLIFIED
                    185461862878543872UL -> SupportedLocales.GERMAN

                    else -> defaultLocale
                }
            }
        }

        chatCommands {
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
