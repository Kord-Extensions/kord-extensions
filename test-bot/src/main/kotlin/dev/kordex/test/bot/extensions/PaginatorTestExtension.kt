/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

@file:Suppress("StringLiteralDuplication")

package dev.kordex.test.bot.extensions

import dev.kordex.core.commands.application.slash.publicSubCommand
import dev.kordex.core.extensions.Extension
import dev.kordex.core.extensions.publicSlashCommand
import dev.kordex.core.i18n.toKey

public class PaginatorTestExtension : Extension() {
	override val name: String = "kordex.test-paginator"

	override suspend fun setup() {
		publicSlashCommand {
			name = "paginator".toKey()
			description = "Paginator testing commands.".toKey()

			publicSubCommand {
				name = "default".toKey()
				description = "Test a default-grouped paginator with pages.".toKey()

				action {
					editingPaginator {
						page {
							description = "Page one!"
						}

						page {
							description = "Page two!"
						}

						page {
							description = "Page three!"
						}
					}.send()
				}
			}

			publicSubCommand {
				name = "chunked".toKey()
				description = "Test a chunked default-group paginator with pages.".toKey()

				action {
					editingPaginator {
						chunkedPages = 3

						page {
							description = "Page one!"
						}

						page {
							description = "Page one!"
						}

						page {
							description = "Page one!"
						}

						page {
							description = "Page two!"
						}

						page {
							description = "Page two!"
						}

						page {
							description = "Page two!"
						}

						page {
							description = "Page three (with only 2 chunks)"
						}

						page {
							description = "Page three (with only 2 chunks)"
						}
					}.send()
				}
			}

			publicSubCommand {
				name = "chunked-small".toKey()
				description = "Test a chunked default-group paginator with one page.".toKey()

				action {
					editingPaginator {
						chunkedPages = 2

						page {
							title = "Page one!"
							description = "Page one!"
						}
					}.send()
				}
			}

			publicSubCommand {
				name = "custom-one".toKey()
				description = "Test a custom-grouped paginator with pages, approach 1.".toKey()

				action {
					editingPaginator("custom".toKey()) {
						page(group = "custom".toKey()) {
							description = "Page one!"
						}

						page(group = "custom".toKey()) {
							description = "Page two!"
						}

						page(group = "custom".toKey()) {
							description = "Page three!"
						}
					}.send()
				}
			}

			publicSubCommand {
				name = "custom-two".toKey()
				description = "Test a custom-grouped paginator with pages, approach 2.".toKey()

				action {
					editingPaginator("custom".toKey()) {
						page("custom".toKey()) {
							description = "Page one!"
						}

						page("custom".toKey()) {
							description = "Page two!"
						}

						page("custom".toKey()) {
							description = "Page three!"
						}
					}.send()
				}
			}

			publicSubCommand {
				name = "custom-pageless".toKey()
				description = "Test a custom-grouped paginator without pages.".toKey()

				action {
					editingPaginator("custom".toKey()) { }.send()
				}
			}
		}
	}
}
