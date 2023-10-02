/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.testbot.extensions

import com.kotlindiscord.kord.extensions.commands.application.slash.ephemeralSubCommand
import com.kotlindiscord.kord.extensions.commands.application.slash.publicSubCommand
import com.kotlindiscord.kord.extensions.components.*
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.common.entity.ChannelType
import dev.kord.core.behavior.channel.asChannelOf
import dev.kord.core.entity.channel.TextChannel

public class SelectorTestExtension : Extension() {
    override val name: String = "Select Menus Test"

    override suspend fun setup() {
        publicSlashCommand {
            name = "selector"
            description = "Test selectors."

            publicSubCommand {
                name = "public"
                description = "Test public selectors."

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
                                        content = selected.map { it.asUser().username }.joinToString("\n")
                                    }
                                }
                            }

                            publicRoleSelectMenu {
                                maximumChoices = null

                                action {
                                    respond {
                                        content = selected.map { it.asRole().name }.joinToString("\n")
                                    }
                                }
                            }

                            publicChannelSelectMenu {
                                maximumChoices = null

                                action {
                                    respond {
                                        content = selected.map { it.id.value }.joinToString("\n")
                                    }
                                }
                            }

                            publicChannelSelectMenu {
                                maximumChoices = null
                                channelType(ChannelType.GuildText)

                                action {
                                    respond {
                                        content = selected.map { it.asChannelOf<TextChannel>().name }.joinToString("\n")
                                    }
                                }
                            }
                        }
                    }
                }
            }

            ephemeralSubCommand {
                name = "ephemeral"
                description = "Test ephemeral selectors."

                action {
                    respond {
                        components {
                            ephemeralStringSelectMenu {
                                option("hi", "1")
                                option("hi hi", "2")
                                maximumChoices = null

                                action {
                                    respond { content = selected.joinToString("\n") }
                                }
                            }

                            ephemeralUserSelectMenu {
                                maximumChoices = null

                                action {
                                    respond {
                                        content = selected.map { it.asUser().username }.joinToString("\n")
                                    }
                                }
                            }

                            ephemeralRoleSelectMenu {
                                maximumChoices = null

                                action {
                                    respond {
                                        content = selected.map { it.asRole().name }.joinToString("\n")
                                    }
                                }
                            }

                            ephemeralChannelSelectMenu {
                                maximumChoices = null

                                action {
                                    respond {
                                        content = selected.map { it.id.value }.joinToString("\n")
                                    }
                                }
                            }

                            ephemeralChannelSelectMenu {
                                maximumChoices = null
                                channelType(ChannelType.GuildText)

                                action {
                                    respond {
                                        content = selected.map { it.asChannelOf<TextChannel>().name }.joinToString("\n")
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
