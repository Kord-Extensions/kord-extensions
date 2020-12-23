package com.kotlindiscord.kord.extensions.test.bot

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.commands.converters.booleanList
import com.kotlindiscord.kord.extensions.commands.converters.defaultingEnum
import com.kotlindiscord.kord.extensions.commands.converters.enum
import com.kotlindiscord.kord.extensions.commands.converters.string
import com.kotlindiscord.kord.extensions.commands.parser.Arguments
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.pagination.Paginator
import com.kotlindiscord.kord.extensions.pagination.pages.Page
import com.kotlindiscord.kord.extensions.pagination.pages.Pages
import dev.kord.core.behavior.channel.createEmbed

class TestExtension(bot: ExtensibleBot) : Extension(bot) {
    override val name = "test"

    class TestArgs : Arguments() {
        val string by string("string")
        val enum by enum<TestEnum>("enum", "test")
        val optionalEnum by defaultingEnum("optional-enum", "test", TestEnum.THREE)
        val bools by booleanList("bools")
    }

    override suspend fun setup() {
        command {
            name = "test"
            description = "Test command, please ignore"

            signature(::TestArgs)

            action {
                with(parse(::TestArgs)) {
                    message.channel.createEmbed {
                        title = "Test response"
                        description = "Test description"

                        field {
                            name = "String"
                            value = string
                        }

                        field {
                            name = "Enum"
                            value = enum.toString()
                        }

                        field {
                            name = "Optional Enum"
                            value = optionalEnum.toString()
                        }

                        field {
                            name = "Bools (${bools.size})"

                            value = if (bools.isEmpty()) {
                                "No elements."
                            } else {
                                bools.joinToString(", ") { "`$it`" }
                            }
                        }
                    }
                }
            }
        }

        command {
            name = "page"
            description = "Paginator test"

            action {
                val pages = Pages()

                (0..2).forEach {
                    pages.addPage(
                        Page(
                            "Short page $it.",
                            footer = "Footer text ($it)"
                        )
                    )

                    pages.addPage(
                        "Expanded",

                        Page(
                            "Expanded page $it, expanded page $it\n" +
                                "Expanded page $it, expanded page $it",
                            footer = "Footer text ($it)"
                        )
                    )

                    pages.addPage(
                        "MASSIVE GROUP",

                        Page(
                            "MASSIVE PAGE $it, MASSIVE PAGE $it\n" +
                                "MASSIVE PAGE $it, MASSIVE PAGE $it\n" +
                                "MASSIVE PAGE $it, MASSIVE PAGE $it\n" +
                                "MASSIVE PAGE $it, MASSIVE PAGE $it\n" +
                                "MASSIVE PAGE $it, MASSIVE PAGE $it",
                            footer = "Footer text ($it)"
                        )
                    )
                }

                Paginator(
                    bot,
                    targetMessage = event.message,
                    pages = pages,
                    keepEmbed = true
                ).send()
            }
        }
    }
}
