/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.test.bot

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.i18n.SupportedLocales
import com.kotlindiscord.kord.extensions.utils.env
import dev.kord.common.entity.Snowflake
import org.koin.core.logger.Level

val TEST_SERVER_ID = Snowflake(787452339908116521UL)

suspend fun main() {
    val bot = ExtensibleBot(env("TOKEN")) {
        koinLogLevel = Level.DEBUG

        i18n {
            localeResolver { _, _, user, _ ->
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
                if (guildId == TEST_SERVER_ID) {
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
