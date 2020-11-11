package com.kotlindiscord.kord.extensions.test.bot

import com.gitlab.kordlib.core.behavior.channel.createEmbed
import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.commands.converters.impl.StringConverter
import com.kotlindiscord.kord.extensions.commands.converters.impl.StringListConverter
import com.kotlindiscord.kord.extensions.commands.parser.Arguments
import com.kotlindiscord.kord.extensions.extensions.Extension

class TestExtension(bot: ExtensibleBot) : Extension(bot) {
    override val name = "test"

    class TestArgs : Arguments() {
        val first by arg("first", StringConverter())
        val second by arg("second", StringListConverter(false))
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
                            name = "First"
                            value = first ?: "null"
                        }

                        field {
                            name = "Second (${second.size})"

                            value = if (second.isEmpty()) {
                                "No elements."
                            } else {
                                second.joinToString(", ") { "`$it`" }
                            }
                        }
                    }
                }
            }
        }
    }
}
