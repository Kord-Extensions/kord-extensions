package com.kotlindiscord.kord.extensions.test.bot

import com.kotlindiscord.kord.extensions.commands.converters.*
import com.kotlindiscord.kord.extensions.commands.cooldowns.impl.GuildCooldown
import com.kotlindiscord.kord.extensions.commands.cooldowns.impl.UserCooldown
import com.kotlindiscord.kord.extensions.commands.parser.Arguments
import com.kotlindiscord.kord.extensions.commands.slash.AutoAckType
import com.kotlindiscord.kord.extensions.commands.slash.converters.enumChoice
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.pagination.Paginator
import com.kotlindiscord.kord.extensions.pagination.pages.Page
import com.kotlindiscord.kord.extensions.pagination.pages.Pages
import com.kotlindiscord.kord.extensions.utils.respond
import dev.kord.common.annotation.KordPreview
import dev.kord.common.entity.Permission
import dev.kord.core.behavior.channel.createEmbed
import kotlin.time.ExperimentalTime
import kotlin.time.seconds

@OptIn(KordPreview::class)
@Suppress("UnderscoresInNumericLiterals")  // They're IDs
class TestExtension : Extension() {
    override val name = "test"

    class TestArgs : Arguments() {
        val string by string("string", "String argument")
        val enum by enum<TestEnum>("enum", "Enum argument", "test")

        val optionalEnum by defaultingEnum(
            "optional-enum",
            "Defaulting enum argument",
            "test",
            TestEnum.THREE
        )

        val bools by booleanList("bools", "Boolean list argument")
    }

    class SlashArgs : Arguments() {
        val string by string("string", "String argument")
        val enum by enum<TestEnum>("enum", "Enum argument", "test")
        val bool by boolean("bool", "Boolean argument")

        val optionalEnum by defaultingEnum(
            "optional-enum",
            "Defaulting enum argument",
            "test",
            TestEnum.THREE
        )
    }

    class SlashChoiceArgs : Arguments() {
        val arg by enumChoice<TestChoiceEnum>("choice", "Enum Choice", "test")
    }

    @OptIn(ExperimentalTime::class)
    override suspend fun setup() {
        slashCommand {
            name = "test-noack"
            description = "Don't auto-ack this one"
            autoAck = AutoAckType.NONE

            guild(787452339908116521) // Our test server

            action {
                ack(false)  // Public ack

                publicFollowUp {
                    embed {
                        title = "An embed!"
                        description = "With a description, and without a content string!"
                    }
                }
            }
        }

        slashCommand(::SlashChoiceArgs) {
            name = "choice"
            description = "Choice-based"
            autoAck = AutoAckType.PUBLIC

            guild(787452339908116521) // Our test server

            action {
                publicFollowUp {
                    content = "Your choice: ${arguments.arg.readableName} -> ${arguments.arg.name}"
                }
            }
        }

        slashCommand {
            name = "group"
            description = "Test command, please ignore"

            guild(787452339908116521) // Our test server

            group("one") {
                description = "Group one"

                subCommand(::SlashArgs) {
                    name = "test"
                    description = "Test command, please ignore"

                    autoAck = AutoAckType.PUBLIC

                    action {
                        publicFollowUp {
                            content = "Some content"

                            embed {
                                title = "Guild response"
                                description = "Guild description"

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

                subCommand {
                    name = "test-two"
                    description = "Test command, please ignore"

                    action {
                        ephemeralFollowUp("Some content")
                    }
                }
            }
        }

        slashCommand {
            name = "guild-embed"
            description = "Test command, please ignore"

            guild(787452339908116521) // Our test server

            group("first") {
                description = "First group."

                subCommand(::SlashArgs) {
                    name = "inner-test"
                    description = "Test command, please ignore"

                    autoAck = AutoAckType.PUBLIC

                    action {
                        publicFollowUp {
                            embed {
                                title = "Guild response"
                                description = "Guild description"

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
            }
        }

        slashCommand(::SlashArgs) {
            name = "test-embed"
            description = "Test command, please ignore\n\n" +

                "Now with some newlines in the description!"

            autoAck = AutoAckType.PUBLIC
            guild(787452339908116521) // Our test server

            action {
                publicFollowUp {
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

        command {
            name = "translation-test"
            description = "Let's test translations."

            action {
                message.respond("Key `test` -> " + translate("test"))
                message.respond("Key `nope` -> " + translate("nope"))
            }
        }

        command {
            name = "requires-perms"
            description = "A command that requires some permissions"

            requirePermissions(Permission.Administrator)

            action {
                message.respond("Looks like I'm an admin. Nice!")
            }
        }

        command(::TestArgs) {
            name = "test"
            description = "Test command, please ignore\n\n" +

                "Now with some newlines in the description!"

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

        command(::TestArgs) {
            name = "test-help"
            description = "Sends help for this command.\n\n" +

                "Now with some newlines in the description!"

            action {
                sendHelp()
            }
        }

        command {
            name = "cooldown-test"
            description = "Cooldown test"

            cooldowns {
                when(it) {
                    is UserCooldown -> 5.seconds
                    else -> null
                }
            }

            action {
                message.respond("There is no cooldown!")
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
                    targetMessage = event.message,
                    pages = pages,
                    keepEmbed = true,
                    locale = getLocale()
                ).send()
            }
        }
    }
}
