package com.kotlindiscord.kord.extensions.testbot.extensions

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import com.kotlindiscord.kord.extensions.components.forms.ModalForm
import com.kotlindiscord.kord.extensions.components.forms.widgets.LineTextWidget
import com.kotlindiscord.kord.extensions.components.forms.widgets.ParagraphTextWidget
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.types.respond

public class ModalTestExtension : Extension() {
    override val name: String = "modals"
    override val bundle: String = "test.strings"

    override suspend fun setup() {
        publicSlashCommand(::Args, ::Modal) {
            name = "modal-public"
            description = "Test the modal functionality."

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

        ephemeralSlashCommand(::Args, ::Modal) {
            name = "modal-ephemeral"
            description = "Test the modal functionality."

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
