/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
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
import dev.kordex.core.i18n.toKey

public class SelectorTestExtension : Extension() {
	override val name: String = "kordex.test-selectors"

	override suspend fun setup() {
		publicSlashCommand {
			name = "selector".toKey()
			description = "Test selectors.".toKey()

			publicSubCommand {
				name = "public".toKey()
				description = "Test public selectors.".toKey()

				action {
					respond {
						components {
							publicStringSelectMenu {
								option("hi".toKey(), "1")
								option("hi hi".toKey(), "2")
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
				name = "mentionable".toKey()
				description = "Test mentionable selectors.".toKey()

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
				name = "ephemeral".toKey()
				description = "Test ephemeral selectors.".toKey()

				action {
					respond {
						components {
							ephemeralStringSelectMenu {
								option("hi".toKey(), "1")
								option("hi hi".toKey(), "2")
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
