/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

@file:Suppress("StringLiteralDuplication")

package dev.kordex.test.bot.extensions

import dev.kordex.core.commands.application.slash.publicSubCommand
import dev.kordex.core.extensions.Extension
import dev.kordex.core.extensions.publicSlashCommand

public class PaginatorTestExtension : Extension() {
	override val name: String = "test-paginator"

	override suspend fun setup() {
		publicSlashCommand {
			name = "paginator"
			description = "Paginator testing commands."

			publicSubCommand {
				name = "default"
				description = "Test a default-grouped paginator with pages."

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
				name = "chunked"
				description = "Test a chunked default-group paginator with pages."

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
				name = "chunked-small"
				description = "Test a chunked default-group paginator with one page."

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
				name = "custom-one"
				description = "Test a custom-grouped paginator with pages, approach 1."

				action {
					editingPaginator("custom") {
						page(group = "custom") {
							description = "Page one!"
						}

						page(group = "custom") {
							description = "Page two!"
						}

						page(group = "custom") {
							description = "Page three!"
						}
					}.send()
				}
			}

			publicSubCommand {
				name = "custom-two"
				description = "Test a custom-grouped paginator with pages, approach 2."

				action {
					editingPaginator("custom") {
						page("custom") {
							description = "Page one!"
						}

						page("custom") {
							description = "Page two!"
						}

						page("custom") {
							description = "Page three!"
						}
					}.send()
				}
			}

			publicSubCommand {
				name = "custom-pageless"
				description = "Test a custom-grouped paginator without pages."

				action {
					editingPaginator("custom") { }.send()
				}
			}
		}
	}
}
