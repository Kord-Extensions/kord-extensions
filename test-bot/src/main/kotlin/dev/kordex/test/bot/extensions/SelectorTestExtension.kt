/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.kordex.test.bot.extensions

import dev.kord.common.entity.ChannelType
import dev.kord.core.behavior.channel.asChannelOf
import dev.kord.core.entity.channel.TextChannel
import dev.kordex.core.commands.application.slash.ephemeralSubCommand
import dev.kordex.core.commands.application.slash.publicSubCommand
import dev.kordex.core.components.*
import dev.kordex.core.extensions.Extension
import dev.kordex.core.extensions.publicSlashCommand

public class SelectorTestExtension : Extension() {
	override val name: String = "kordex.test-selectors"

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
				name = "mentionable"
				description = "Test mentionable selectors."

				action {
					respond {
						components {
							ephemeralMentionableSelectMenu {
								maximumChoices = null

								action {
									respond {
										content = buildString {
											if (selected.isNotEmpty()) {
												if (selectedRoles.isNotEmpty()) {
													appendLine("# Roles")

													selectedRoles.forEach {
														appendLine("- `${it.id}` -> ${it.asRoleOrNull()?.name}")
													}

													appendLine()
												}

												if (selectedUsers.isNotEmpty()) {
													appendLine("# Users")

													selectedUsers.forEach {
														appendLine("- `${it.id}` -> ${it.asUserOrNull()?.tag}")
													}

													appendLine()
												}
											} else {
												append("Nothing selected.")
											}
										}
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
