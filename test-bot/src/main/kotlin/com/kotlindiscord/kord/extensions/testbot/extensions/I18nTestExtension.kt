/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

@file:Suppress("StringLiteralDuplication")

package com.kotlindiscord.kord.extensions.testbot.extensions

import com.kotlindiscord.kord.extensions.commands.application.slash.group
import com.kotlindiscord.kord.extensions.commands.application.slash.publicSubCommand
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.types.respond

public class I18nTestExtension : Extension() {
    override val name: String = "test-i18n"
    override val bundle: String = "test"

    override suspend fun setup() {
        publicSlashCommand {
            name = "command.banana-flat"
            description = "Translated banana"

            action {
                val commandLocale = getLocale()
                val interactionLocale = event.interaction.locale?.asJavaLocale()

                assert(commandLocale == interactionLocale) {
                    "Command locale (`$commandLocale`) does not match interaction locale (`$interactionLocale`)"
                }

                respond { content = "Text: ${translate("command.banana")}" }
            }
        }

        publicSlashCommand {
            name = "command.banana-sub"
            description = "Translated banana subcommand"

            publicSubCommand {
                name = "command.banana"
                description = "Translated banana"

                action {
                    val commandLocale = getLocale()
                    val interactionLocale = event.interaction.locale?.asJavaLocale()

                    assert(commandLocale == interactionLocale) {
                        "Command locale (`$commandLocale`) does not match interaction locale (`$interactionLocale`)"
                    }

                    respond { content = "Text: ${translate("command.banana")}" }
                }
            }
        }

        publicSlashCommand {
            name = "command.banana-group"
            description = "Translated banana group"

            group("command.banana") {
                description = "Translated banana group"

                publicSubCommand {
                    name = "command.banana"
                    description = "Translated banana"

                    action {
                        val commandLocale = getLocale()
                        val interactionLocale = event.interaction.locale?.asJavaLocale()

                        assert(commandLocale == interactionLocale) {
                            "Command locale (`$commandLocale`) does not match interaction locale (`$interactionLocale`)"
                        }

                        respond { content = "Text: ${translate("command.banana")}" }
                    }
                }
            }
        }
    }
}
