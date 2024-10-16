/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.test.bot.extensions

import dev.kordex.core.commands.Arguments
import dev.kordex.core.commands.application.slash.publicSubCommand
import dev.kordex.core.commands.converters.impl.string
import dev.kordex.core.components.components
import dev.kordex.core.components.forms.ModalForm
import dev.kordex.core.components.forms.widgets.LineTextWidget
import dev.kordex.core.components.forms.widgets.ParagraphTextWidget
import dev.kordex.core.components.publicButton
import dev.kordex.core.extensions.Extension
import dev.kordex.core.extensions.publicMessageCommand
import dev.kordex.core.extensions.publicSlashCommand
import dev.kordex.core.extensions.publicUserCommand
import dev.kordex.core.i18n.toKey
import dev.kordex.core.i18n.types.Key
import dev.kordex.test.bot.Translations

public class ModalTestExtension : Extension() {
	override val name: String = "kordex.modals"

	@Suppress("StringLiteralDuplication")
	override suspend fun setup() {
		publicUserCommand(::Modal) {
			name = "Modal".toKey()

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
			name = "Modal".toKey()

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
			name = "modals".toKey()
			description = "Modal testing commands".toKey()

			publicSubCommand {
				name = "button".toKey()
				description = "Test a modal response to a button".toKey()

				action {
					respond {
						components {
							publicButton(::Modal) {
								label = "Modal!".toKey()

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
				name = "command".toKey()
				description = "Test a modal response to a command".toKey()

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
			name = "string".toKey()
			description = "A string argument".toKey()
		}
	}

	public inner class Modal : ModalForm() {
		override var title: Key = Translations.Modal.title

		public val line: LineTextWidget = lineText {
			label = Translations.Modal.line
			placeholder = Translations.Modal.Line.placeholder
		}

		public val paragraph: ParagraphTextWidget = paragraphText {
			label = Translations.Modal.paragraph
			placeholder = Translations.Modal.Paragraph.placeholder
		}
	}
}
