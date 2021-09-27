@file:OptIn(KordPreview::class)

package com.kotlindiscord.kord.extensions.test.bot

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.converters.impl.enumChoice
import com.kotlindiscord.kord.extensions.commands.application.slash.ephemeralSubCommand
import com.kotlindiscord.kord.extensions.commands.application.slash.group
import com.kotlindiscord.kord.extensions.commands.application.slash.publicSubCommand
import com.kotlindiscord.kord.extensions.commands.converters.impl.*
import com.kotlindiscord.kord.extensions.components.*
import com.kotlindiscord.kord.extensions.components.types.emoji
import com.kotlindiscord.kord.extensions.extensions.*
import com.kotlindiscord.kord.extensions.pagination.MessageButtonPaginator
import com.kotlindiscord.kord.extensions.pagination.pages.Page
import com.kotlindiscord.kord.extensions.pagination.pages.Pages
import com.kotlindiscord.kord.extensions.types.editingPaginator
import com.kotlindiscord.kord.extensions.types.respond
import com.kotlindiscord.kord.extensions.utils.respond
import dev.kord.common.annotation.KordPreview
import dev.kord.common.entity.ButtonStyle
import dev.kord.common.entity.Permission
import dev.kord.core.behavior.channel.createEmbed
import dev.kord.core.behavior.reply
import dev.kord.core.event.guild.GuildCreateEvent
import dev.kord.rest.builder.message.create.embed
import mu.KotlinLogging

// They're IDs
@Suppress("UnderscoresInNumericLiterals")
class TestExtension : Extension() {
    override val name = "test"

    val logger = KotlinLogging.logger {}

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
        event<GuildCreateEvent> {
            action {
                logger.info { "Guild created: ${event.guild.name} (${event.guild.id.asString})" }
            }
        }

        publicMessageCommand {
            name = "Raw Info"

            check {
                failIf("This message command only supports non-webhook, non-interaction messages.") {
                    event.interaction.messages?.values?.firstOrNull()?.author == null
                }
            }

            action {
                val message = targetMessages.firstOrNull() ?: return@action

                respond {
                    content = "**Message command:** Raw content for message sent by ${message.author!!.mention}"

                    embed {
                        description = "```markdown\n${message.content}```"
                    }
                }
            }
        }

        publicUserCommand {
            name = "ping"

            check {
                failIf("That's me, you can't make me ping myself!") {
                    event.interaction.users?.values?.firstOrNull()?.id == kord.selfId
                }
            }

            action {
                val user = targetUsers.firstOrNull() ?: return@action

                respond {
                    content = "Let's ping ${user.mention} for no reason. <3"
                }
            }
        }

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

                    components {
                        publicSelectMenu {
                            maximumChoices = null

                            option("Option 1", "one")
                            option("Option 2", "two")
                            option("Option 3", "three")

                            action {
                                respond {
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

        ephemeralSlashCommand {
            name = "pages"
            description = "Pages!"

            guild(787452339908116521)

            action {
                editingPaginator("short") {
                    owner = event.interaction.user.asUser()
                    timeoutSeconds = 60
                    keepEmbed = false

                    (0..2).forEach {
                        page {
                            description = "Short page $it."

                            footer {
                                text = "Footer text ($it)"
                            }
                        }

                        page("Expanded") {
                            description = "Expanded page $it, expanded page $it\n" +
                                "Expanded page $it, expanded page $it"

                            footer {
                                text = "Footer text ($it)"
                            }
                        }

                        page("MASSIVE GROUP") {
                            description = "MASSIVE PAGE $it, MASSIVE PAGE $it\n" +
                                "MASSIVE PAGE $it, MASSIVE PAGE $it\n" +
                                "MASSIVE PAGE $it, MASSIVE PAGE $it\n" +
                                "MASSIVE PAGE $it, MASSIVE PAGE $it\n" +
                                "MASSIVE PAGE $it, MASSIVE PAGE $it"

                            footer {
                                text = "Footer text ($it)"
                            }
                        }
                    }
                }.send()
            }
        }

        ephemeralSlashCommand {
            name = "buttons"
            description = "Buttons!"

            guild(787452339908116521) // Our test server

            action {
                respond {
                    content = "Buttons!"

                    components {
                        ephemeralButton {
                            label = "Button one!"

                            action {
                                respond { content = "Button one pressed!" }
                            }
                        }

                        ephemeralButton {
                            label = "Button two!"
                            style = ButtonStyle.Secondary

                            action {
                                respond { content = "Button two pressed!" }
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

        publicSlashCommand {
            name = "test-noack"
            description = "Don't auto-ack this one"

            guild(787452339908116521) // Our test server

            initialResponse {
                embed {
                    title = "An embed!"
                    description = "With a description, and without a content string!"
                }
            }

            action {
            }
        }

        publicSlashCommand(::SlashChoiceArgs) {
            name = "choice"
            description = "Choice-based"

            guild(787452339908116521) // Our test server

            action {
                respond {
                    content = "Your choice: ${arguments.arg.readableName} -> ${arguments.arg.name}"
                }
            }
        }

        ephemeralSlashCommand {
            name = "group"
            description = "Test command, please ignore"

            guild(787452339908116521) // Our test server

            group("one") {
                description = "Group one"

                publicSubCommand(::SlashArgs) {
                    name = "test"
                    description = "Test command, please ignore"

                    action {
                        respond {
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

                ephemeralSubCommand {
                    name = "test-two"
                    description = "Test command, please ignore"

                    action {
                        respond {
                            content = "Some content"
                        }
                    }
                }
            }
        }

        ephemeralSlashCommand {
            name = "guild-embed"
            description = "Test command, please ignore"

            guild(787452339908116521) // Our test server

            group("first") {
                description = "First group."

                publicSubCommand(::SlashArgs) {
                    name = "inner-test"
                    description = "Test command, please ignore"

                    action {
                        respond {
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

        publicSlashCommand(::SlashArgs) {
            name = "test-embed"
            description = "Test command, please ignore\n\n" +

                "Now with some newlines in the description!"

            guild(787452339908116521) // Our test server

            action {
                respond {
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

            requireBotPermissions(Permission.Administrator)

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
