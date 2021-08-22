@file:OptIn(KordPreview::class, TranslationNotSupported::class)

package com.kotlindiscord.kord.extensions.test.bot

import com.kotlindiscord.kord.extensions.commands.converters.impl.*
import com.kotlindiscord.kord.extensions.commands.parser.Arguments
import com.kotlindiscord.kord.extensions.commands.slash.AutoAckType
import com.kotlindiscord.kord.extensions.commands.slash.TranslationNotSupported
import com.kotlindiscord.kord.extensions.commands.slash.converters.impl.enumChoice
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.chatCommand
import com.kotlindiscord.kord.extensions.extensions.chatGroupCommand
import com.kotlindiscord.kord.extensions.extensions.slashCommand
import com.kotlindiscord.kord.extensions.pagination.MessageButtonPaginator
import com.kotlindiscord.kord.extensions.pagination.pages.Page
import com.kotlindiscord.kord.extensions.pagination.pages.Pages
import com.kotlindiscord.kord.extensions.utils.respond
import dev.kord.common.annotation.KordPreview
import dev.kord.common.entity.ButtonStyle
import dev.kord.common.entity.Permission
import dev.kord.core.behavior.channel.createEmbed
import dev.kord.core.behavior.reply
import dev.kord.rest.builder.message.create.embed

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
        chatCommand(::ColorArgs) {
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

        chatCommand(::MessageArgs) {
            name = "msg"
            description = "Message argument test"

            action {
                arguments.message.reply {
                    content = "Replied to message."
                }
            }
        }

        chatCommand(::CoalescedArgs) {
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

        chatCommand {
            name = "dropdown"
            description = "Dropdown test!"

            action {
                message.respond {
                    content = "Here's a dropdown."

                    components(60) {
                        menu {
                            autoAck = AutoAckType.PUBLIC
                            maximumChoices = null

                            option("Option 1", "one")
                            option("Option 2", "two")
                            option("Option 3", "three")

                            action {
                                publicFollowUp {
                                    content = "You picked the following options: " + selected.joinToString {
                                        "`$it`"
                                    }
                                }
                            }
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
                paginator("short") {
                    owner = event.interaction.user.asUser()
                    timeoutSeconds = 60
                    keepEmbed = false

                    (0..2).forEach {
                        page(
                            Page {
                                description = "Short page $it."

                                footer {
                                    text = "Footer text ($it)"
                                }
                            }
                        )

                        page(
                            "Expanded",

                            Page {
                                description = "Expanded page $it, expanded page $it\n" +
                                    "Expanded page $it, expanded page $it"

                                footer {
                                    text = "Footer text ($it)"
                                }
                            }
                        )

                        page(
                            "MASSIVE GROUP",

                            Page {
                                description = "MASSIVE PAGE $it, MASSIVE PAGE $it\n" +
                                    "MASSIVE PAGE $it, MASSIVE PAGE $it\n" +
                                    "MASSIVE PAGE $it, MASSIVE PAGE $it\n" +
                                    "MASSIVE PAGE $it, MASSIVE PAGE $it\n" +
                                    "MASSIVE PAGE $it, MASSIVE PAGE $it"

                                footer {
                                    text = "Footer text ($it)"
                                }
                            }
                        )
                    }
                }.send()
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

        chatCommand {
            name = "translation-test"
            description = "Let's test translations."

            action {
                message.respond("Key `test` -> " + translate("test"))
                message.respond("Key `nope` -> " + translate("nope"))
            }
        }

        chatCommand {
            name = "requires-perms"
            description = "A command that requires some permissions"

            requirePermissions(Permission.Administrator)

            action {
                message.respond("Looks like I'm an admin. Nice!")
            }
        }

        chatCommand(::TestArgs) {
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

        chatCommand(::TestArgs) {
            name = "test-help"
            description = "Sends help for this command.\n\n" +

                "Now with some newlines in the description!"

            action {
                sendHelp()
            }
        }

        chatCommand {
            name = "page"
            description = "Paginator test"

            action {
                paginator("short", targetMessage = event.message) {
                    keepEmbed = true
                    owner = user
                    locale = getLocale()

                    (0..2).forEach {
                        page(
                            "short",

                            Page {
                                description = "Short page $it."

                                footer {
                                    text = "Footer text ($it)"
                                }
                            }
                        )

                        page(
                            "expanded",

                            Page {
                                description = "Expanded page $it, expanded page $it\n" +
                                    "Expanded page $it, expanded page $it"

                                footer {
                                    text = "Footer text ($it)"
                                }
                            }
                        )

                        page(
                            "massive",

                            Page {
                                description = "MASSIVE PAGE $it, MASSIVE PAGE $it\n" +
                                    "MASSIVE PAGE $it, MASSIVE PAGE $it\n" +
                                    "MASSIVE PAGE $it, MASSIVE PAGE $it\n" +
                                    "MASSIVE PAGE $it, MASSIVE PAGE $it\n" +
                                    "MASSIVE PAGE $it, MASSIVE PAGE $it"

                                footer {
                                    text = "Footer text ($it)"
                                }
                            }
                        )
                    }
                }.send()
            }
        }

        chatCommand {
            name = "page2"
            description = "Paginator test 2"

            action {
                val pages = Pages()

                (0..2).forEach {
                    pages.addPage(
                        Page {
                            description = "Short page $it."

                            footer {
                                text = "Footer text ($it)"
                            }
                        }
                    )

                    pages.addPage(
                        "Expanded",

                        Page {
                            description = "Expanded page $it, expanded page $it\n" +
                                "Expanded page $it, expanded page $it"

                            footer {
                                text = "Footer text ($it)"
                            }
                        }
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

        chatGroupCommand {
            name = "group"
            description = "Command group"

            chatCommand {
                name = "one"
                description = "one"

                action {
                    message.respond("One!")
                }
            }

            chatCommand {
                name = "two"
                description = "two"

                action {
                    message.respond("Two!")
                }
            }

            chatCommand {
                name = "three"
                description = "three"

                action {
                    message.respond("Three!")
                }
            }
        }
    }
}
