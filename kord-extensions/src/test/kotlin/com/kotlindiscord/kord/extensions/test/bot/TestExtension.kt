@file:OptIn(KordPreview::class, TranslationNotSupported::class)

package com.kotlindiscord.kord.extensions.test.bot

import com.kotlindiscord.kord.extensions.commands.converters.impl.*
import com.kotlindiscord.kord.extensions.commands.parser.Arguments
import com.kotlindiscord.kord.extensions.commands.slash.AutoAckType
import com.kotlindiscord.kord.extensions.commands.slash.TranslationNotSupported
import com.kotlindiscord.kord.extensions.commands.slash.converters.impl.enumChoice
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.pagination.InteractionButtonPaginator
import com.kotlindiscord.kord.extensions.pagination.MessageButtonPaginator
import com.kotlindiscord.kord.extensions.pagination.pages.Page
import com.kotlindiscord.kord.extensions.pagination.pages.Pages
import com.kotlindiscord.kord.extensions.utils.respond
import dev.kord.common.annotation.KordPreview
import dev.kord.common.entity.ButtonStyle
import dev.kord.common.entity.Permission
import dev.kord.core.behavior.channel.createEmbed
import dev.kord.core.behavior.reply
import dev.kord.rest.builder.interaction.embed

// They're IDs
@Suppress("UnderscoresInNumericLiterals")
class TestExtension : Extension() {
    override val name = "test"

    class ColorArgs : Arguments() {
        val color by colour("color", "Color to use for the embed")
    }

    class TestArgs : Arguments() {
        val string by string("string", "String argument")
        val enum by enum<TestEnum>("enum", "Enum argument", "test")

        val optionalEnum by defaultingEnum(
            displayName = "optional-enum",
            description = "Defaulting enum argument",
            typeName = "test",
            defaultValue = TestEnum.THREE
        )

        val bools by booleanList("bools", "Boolean list argument")
    }

    class SlashArgs : Arguments() {
        val string by string("string", "String argument")
        val enum by enum<TestEnum>("enum", "Enum argument", "test")
        val bool by boolean("bool", "Boolean argument")

        val optionalEnum by defaultingEnum(
            displayName = "optional-enum",
            description = "Defaulting enum argument",
            typeName = "test",
            defaultValue = TestEnum.THREE
        )
    }

    class SlashChoiceArgs : Arguments() {
        val arg by enumChoice<TestChoiceEnum>("choice", "Enum Choice", "test")
    }

    class CoalescedArgs : Arguments() {
        val string by coalescedString("input", "Text to use")
        val flag by optionalBoolean("flag", "Some kinda flag")
    }

    class MessageArgs : Arguments() {
        val message by message("target", "Target message")
    }

    override suspend fun setup() {
        command(::ColorArgs) {
            name = "color"
            aliases = arrayOf("colour")
            description = "Get an embed with a set color"

            action {
                message.respond {
                    embed {
                        description = "Here's your embed!"

                        color = arguments.color
                    }
                }
            }
        }

        command(::MessageArgs) {
            name = "msg"
            description = "Message argument test"

            action {
                arguments.message.reply {
                    content = "Replied to message."
                }
            }
        }

        command(::CoalescedArgs) {
            name = "coalesce"
            description = "Coalesce me, baby"

            action {
                message.respond {
                    embed {
                        description = arguments.string

                        field {
                            name = "flag"
                            value = arguments.flag.toString()
                        }
                    }
                }
            }
        }

        slashCommand {
            name = "pages"
            description = "Pages!"
            autoAck = AutoAckType.PUBLIC

            guild(787452339908116521)

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

                val paginator = InteractionButtonPaginator(
                    extension = this@TestExtension,
                    pages = pages,
                    owner = event.interaction.user.asUser(),
                    timeoutSeconds = 60,
                    parentContext = this,
                    keepEmbed = false
                )

                paginator.send()
            }
        }

        slashCommand {
            name = "buttons"
            description = "Buttons!"

            guild(787452339908116521) // Our test server

            action {
                ephemeralFollowUp {
                    content = "Buttons!"

                    components(60) {
                        interactiveButton {
                            label = "Button one!"

                            action {
                                respond("Button one pressed!")
                            }
                        }

                        interactiveButton {
                            label = "Button two!"
                            style = ButtonStyle.Secondary

                            action {
                                respond("Button two pressed!")
                            }
                        }

                        disabledButton {
                            emoji("❎")
                        }

                        linkButton {
                            label = "Google"
                            emoji("🔗")

                            url = "https://google.com"
                        }
                    }
                }
            }
        }

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
                        ephemeralFollowUp {
                            content = "Some content"
                        }
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
            name = "page"
            description = "Paginator test"

            action {
                val pages = Pages(defaultGroup = "short")

                (0..2).forEach {
                    pages.addPage(
                        "short",

                        Page(
                            "Short page $it.",
                            footer = "Footer text ($it)"
                        )
                    )

                    pages.addPage(
                        "expanded",

                        Page(
                            "Expanded page $it, expanded page $it\n" +
                                "Expanded page $it, expanded page $it",
                            footer = "Footer text ($it)"
                        )
                    )

                    pages.addPage(
                        "massive",

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

                MessageButtonPaginator(
                    extension = this@TestExtension,
                    targetMessage = event.message,
                    pages = pages,
                    keepEmbed = true,
                    owner = user,
                    locale = getLocale()
                ).send()
            }
        }

        command {
            name = "page2"
            description = "Paginator test 2"

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
                }

                MessageButtonPaginator(
                    extension = this@TestExtension,
                    targetMessage = event.message,
                    pages = pages,
                    keepEmbed = false,
                    owner = user,
                    locale = getLocale()
                ).send()
            }
        }

        group {
            name = "group"
            description = "Command group"

            command {
                name = "one"
                description = "one"

                action {
                    message.respond("One!")
                }
            }

            command {
                name = "two"
                description = "two"

                action {
                    message.respond("Two!")
                }
            }

            command {
                name = "three"
                description = "three"

                action {
                    message.respond("Three!")
                }
            }
        }
    }
}
