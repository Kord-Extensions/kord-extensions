/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

@file:Suppress("StringLiteralDuplication")

package com.kotlindiscord.kord.extensions.testbot.extensions

import com.kotlindiscord.kord.extensions.commands.application.slash.publicSubCommand
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand

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
