/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.testbot.extensions

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.publicSubCommand
import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import com.kotlindiscord.kord.extensions.components.components
import com.kotlindiscord.kord.extensions.components.forms.ModalForm
import com.kotlindiscord.kord.extensions.components.forms.widgets.LineTextWidget
import com.kotlindiscord.kord.extensions.components.forms.widgets.ParagraphTextWidget
import com.kotlindiscord.kord.extensions.components.publicButton
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.publicMessageCommand
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.extensions.publicUserCommand
import com.kotlindiscord.kord.extensions.types.respond

public class ModalTestExtension : Extension() {
    override val name: String = "modals"
    override val bundle: String = "test.strings"

    @Suppress("StringLiteralDuplication")
    override suspend fun setup() {
        publicUserCommand(::Modal) {
            name = "Modal"

            action { modal ->
                respond {
                    content = buildString {
                        if (modal == null) {
                            append("**No modal found!**")

                            return@buildString
                        }

                        append("**Line:** `")
                        appendLine(modal.line.value)
                        append("`")
                        appendLine()

                        appendLine("**Paragraph:** ```")
                        appendLine(modal.paragraph.value)
                        append("```")
                        appendLine()
                    }
                }
            }
        }

        publicMessageCommand(::Modal) {
            name = "Modal"

            action { modal ->
                respond {
                    content = buildString {
                        if (modal == null) {
                            append("**No modal found!**")

                            return@buildString
                        }

                        append("**Line:** `")
                        appendLine(modal.line.value)
                        append("`")
                        appendLine()

                        appendLine("**Paragraph:** ```")
                        appendLine(modal.paragraph.value)
                        append("```")
                        appendLine()
                    }
                }
            }
        }

        publicSlashCommand {
            name = "modals"
            description = "Modal testing commands"

            publicSubCommand {
                name = "button"
                description = "Test a modal response to a button"

                action {
                    respond {
                        components {
                            publicButton(::Modal) {
                                bundle = "test.strings"
                                label = "Modal!"

                                action { modal ->
                                    respond {
                                        content = buildString {
                                            if (modal == null) {
                                                append("**No modal found!**")

                                                return@buildString
                                            }

                                            append("**Line:** `")
                                            appendLine(modal.line.value)
                                            append("`")
                                            appendLine()

                                            appendLine("**Paragraph:** ```")
                                            appendLine(modal.paragraph.value)
                                            append("```")
                                            appendLine()
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            publicSubCommand(::Args, ::Modal) {
                name = "command"
                description = "Test a modal response to a command"

                action { modal ->
                    respond {
                        content = buildString {
                            append("**Argument:** `")
                            appendLine(arguments.str)
                            append("`")
                            appendLine()

                            if (modal == null) {
                                append("**No modal found!**")

                                return@buildString
                            }

                            append("**Line:** `")
                            appendLine(modal.line.value)
                            append("`")
                            appendLine()

                            appendLine("**Paragraph:** ```")
                            appendLine(modal.paragraph.value)
                            append("```")
                            appendLine()
                        }
                    }
                }
            }
        }
    }

    public inner class Args : Arguments() {
        public val str: String by string {
            name = "string"
            description = "A string argument"
        }
    }

    public inner class Modal : ModalForm() {
        override var title: String = "modal.title"

        public val line: LineTextWidget = lineText {
            label = "modal.line"
            placeholder = "modal.line.placeholder"
        }

        public val paragraph: ParagraphTextWidget = paragraphText {
            label = "modal.paragraph"
            placeholder = "modal.paragraph.placeholder"
        }
    }
}
