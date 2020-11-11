package com.kotlindiscord.kord.extensions.test.bot

import com.kotlindiscord.kord.extensions.ExtensibleBot

suspend fun main() {
    val bot = ExtensibleBot(System.getenv("TOKEN"), "!")

    bot.addExtension(TestExtension::class)

    bot.start()
}
