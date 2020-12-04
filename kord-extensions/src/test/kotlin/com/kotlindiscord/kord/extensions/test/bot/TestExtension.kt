package com.kotlindiscord.kord.extensions.test.bot

import dev.kord.core.behavior.channel.createEmbed
import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.commands.converters.*
import com.kotlindiscord.kord.extensions.commands.parser.Arguments
import com.kotlindiscord.kord.extensions.extensions.Extension

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
    }
}
