package com.kotlindiscord.kord.extensions.test.bot

import com.kotlindiscord.kord.extensions.ExtensibleBot
import org.koin.core.logger.Level

suspend fun main() {
    val bot = ExtensibleBot(
        System.getenv("TOKEN"),
        "!",
        koinLogLevel = Level.DEBUG
    )

    bot.addExtension(::TestExtension)

    bot.start()
}
