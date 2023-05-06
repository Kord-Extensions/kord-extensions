/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

@file:Suppress("StringLiteralDuplication")

package com.kotlindiscord.kord.extensions.testbot.extensions

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.*
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import com.kotlindiscord.kord.extensions.utils.suggestStringMap
import dev.kord.common.entity.ChannelType
import dev.kord.common.entity.ForumTag
import dev.kord.core.behavior.channel.asChannelOfOrNull
import dev.kord.core.entity.Attachment
import dev.kord.core.entity.channel.Channel

public class ArgumentTestExtension : Extension() {
    override val name: String = "test-args"

    override suspend fun setup() {
        publicSlashCommand(::TagArgs) {
            name = "test-tag"
            description = "Test the tags converter"

            action {
                respond {
                    content = "Tag provided: `${arguments.tag?.name}`"
                }
            }
        }

        publicSlashCommand(::OptionalArgs) {
            name = "optional-autocomplete"
            description = "Check whether autocomplete works with an optional converter."

            action {
                respond {
                    content = "You provided: `${arguments.response}`"
                }
            }
        }

        publicSlashCommand(::LengthConstrainedArgs) {
            name = "length-constrained"
            description = "Check if length limits work"

            action {
                respond {
                    content = buildString {
                        append("You name is: `${arguments.name}`")
                        arguments.lastName?.let {
                            append(" `$it`")
                        }
                    }
                }
            }
        }

        publicSlashCommand(::AttachmentArguments) {
            name = "attachment"
            description = "Check attachment command options."

            action {
                respond {
                    content = buildString {
                        append("You attached: ${arguments.file.filename}.")

                        arguments.optionalFile?.let {
                            append("\nYou also attached: ${it.filename}")
                        }
                    }
                }
            }
        }

        publicSlashCommand(::ChannelArguments) {
            name = "channel"
            description = "Check channel command options."

            action {
                respond {
                    content = buildString {
                        append("You specified: ${arguments.channel.mention}.")
                    }
                }
            }
        }
    }

    public inner class TagArgs : Arguments() {
        override val parseForAutocomplete: Boolean = true

        public val channel: Channel? by optionalChannel {
            name = "channel"
            description = "Channel to select a tag from"

            requireChannelType(ChannelType.GuildForum)
        }

        public val tag: ForumTag? by optionalTag {
            name = "tag"
            description = "Tag to use"

            channelGetter = {
                channel?.asChannelOfOrNull()
            }
        }
    }

    public inner class OptionalArgs : Arguments() {
        public val response: String? by optionalString {
            name = "response"
            description = "Text to receive"

            autoComplete {
                suggestStringMap(
                    mapOf(
                        "one" to "One",
                        "two" to "Two",
                        "three" to "Three"
                    )
                )
            }
        }
    }

    public inner class LengthConstrainedArgs : Arguments() {
        public val name: String by string {
            name = "name"
            description = "The user's name."
            minLength = 3
            maxLength = 10
        }

        public val lastName: String? by optionalString {
            name = "last_name"
            description = "The user's last name."
            minLength = 4
            maxLength = 15
        }
    }

    public inner class AttachmentArguments : Arguments() {
        public val file: Attachment by attachment {
            name = "file"
            description = "An attached file."
        }

        public val optionalFile: Attachment? by optionalAttachment {
            name = "optional_file"
            description = "An optional file."
        }
    }

    public inner class ChannelArguments : Arguments() {
        public val channel: Channel by channel {
            name = "channel"
            description = "A text channel"

            requireChannelType(ChannelType.GuildText)
        }
    }
}
