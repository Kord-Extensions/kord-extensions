/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.testbot.extensions

import com.kotlindiscord.kord.extensions.components.*
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.common.entity.ChannelType
import dev.kord.core.behavior.channel.asChannelOf
import dev.kord.core.entity.channel.TextChannel
import kotlinx.coroutines.runBlocking

public class SelectorTestExtension : Extension() {
    override val name: String = "Select Menus Test"

    override suspend fun setup() {
        publicSlashCommand {
            name = "selector"
            description = "Test selectors."

            action {
                respond {
                    components {
                        publicStringSelectMenu {
                            option("hi", "1")
                            option("hi hi", "2")
                            maximumChoices = null

                            action {
                                respond { content = selected.joinToString("\n") }
                            }
                        }

                        publicUserSelectMenu {
                            maximumChoices = null

                            action {
                                respond {
                                    content = selected.joinToString("\n") {
                                        runBlocking { it.asUser().username }
                                    }
                                }
                            }
                        }

                        publicRoleSelectMenu {
                            maximumChoices = null

                            action {
                                respond {
                                    content = selected.joinToString("\n") {
                                        runBlocking { it.asRole().name }
                                    }
                                }
                            }
                        }

                        publicChannelSelectMenu {
                            maximumChoices = null

                            action {
                                respond {
                                    content = selected.joinToString("\n") {
                                        runBlocking { it.asChannel().id.value.toString() }
                                    }
                                }
                            }
                        }

                        publicChannelSelectMenu {
                            maximumChoices = null
                            channelType(ChannelType.GuildText)

                            action {
                                respond {
                                    content = selected.joinToString("\n") {
                                        runBlocking { it.asChannelOf<TextChannel>().name }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
