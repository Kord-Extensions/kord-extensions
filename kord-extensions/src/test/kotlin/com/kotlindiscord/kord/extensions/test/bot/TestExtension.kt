package com.kotlindiscord.kord.extensions.test.bot

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.commands.converters.*
import com.kotlindiscord.kord.extensions.commands.parser.Arguments
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.pagination.Paginator
import com.kotlindiscord.kord.extensions.pagination.pages.Page
import com.kotlindiscord.kord.extensions.pagination.pages.Pages
import dev.kord.common.annotation.KordPreview
import dev.kord.core.behavior.channel.createEmbed

@OptIn(KordPreview::class)
class TestExtension(bot: ExtensibleBot) : Extension(bot) {
    override val name = "test"

    class TestArgs : Arguments() {
        val string by string("string", "String argument")
        val enum by enum<TestEnum>("enum", "Enum argument", "test")

        val optionalEnum by defaultingEnum(
            "optional-enum", "Defaulting enum argument", "test", TestEnum.THREE
        )

        val bools by booleanList("bools", "Boolean list argument")
    }

    class SlashArgs : Arguments() {
        val string by string("string", "String argument")
        val enum by enum<TestEnum>("enum", "Enum argument", "test")
        val bool by boolean("bool", "Boolean argument")

        val optionalEnum by defaultingEnum(
            "optional-enum", "Defaulting enum argument", "test", TestEnum.THREE
        )
    }

    override suspend fun setup() {
        slashCommand {
            name = "test-noack"
            description = "Don't auto-ack this one"
            autoAck = false
            showSource = true

            action {
                ack(true, "") {
                    embed {
                        this.title = "An embed!"
                        this.description = "With a description, and without a content string!"
                    }
                }
            }
        }

        slashCommand(::SlashArgs) {
            name = "test-embed"
            description = "Test command, please ignore"
            showSource = true

            action {
                followUp {
                    embed {
                        title = "Test response"
                        description = "Test description"

                        field {
                            name = "String"
                            value = arguments.string
                        }

                        field {
                            name = "Enum"
                            value = arguments.enum.toString()
                        }

                        field {
                            name = "Optional Enum"
                            value = arguments.optionalEnum.toString()
                        }

                        field {
                            name = "Bool"

                            value = arguments.bool.toString()
                        }
                    }
                }
            }
        }

        command(::TestArgs) {
            name = "test"
            description = "Test command, please ignore"

            action {
                with(arguments) {
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
