/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.extensions.impl

import dev.kord.core.behavior.reply
import dev.kord.core.builder.components.emoji
import dev.kord.core.entity.ReactionEmoji
import dev.kord.rest.builder.message.actionRow
import dev.kord.rest.builder.message.create.MessageCreateBuilder
import dev.kord.rest.builder.message.embed
import dev.kordex.core.builders.ExtensibleBotBuilder
import dev.kordex.core.commands.CommandContext
import dev.kordex.core.commands.application.slash.ephemeralSubCommand
import dev.kordex.core.commands.application.slash.publicSubCommand
import dev.kordex.core.extensions.Extension
import dev.kordex.core.extensions.chatCommand
import dev.kordex.core.extensions.chatGroupCommand
import dev.kordex.core.extensions.ephemeralSlashCommand
import dev.kordex.core.extensions.publicSlashCommand
import org.koin.core.component.inject

@Suppress("StringLiteralDuplication")
public class AboutExtension : Extension() {
	override val name: String = "kordex.about"

	private val settings: ExtensibleBotBuilder by inject()

	override suspend fun setup() {
		val ephemeral = settings.aboutBuilder.ephemeral

		if (settings.aboutBuilder.sections.isEmpty()) {
			chatCommand {
				name = "extensions.about.commandName"
				description = "extensions.about.commandDescription"

				action {
					message.reply {
						addAbout(this@action)
					}
				}
			}
		} else {
			chatGroupCommand {
				name = "extensions.about.commandName"
				description = "extensions.about.commandDescription"

				this.chatCommand {
					name = "extensions.about.generalCommandName"
					description = "extensions.about.generalCommandDescription"

					action {
						message.reply {
							addAbout(this@action)
						}
					}
				}

				settings.aboutBuilder.sections.forEach { section ->
					this.chatCommand {
						name = section.name
						description = section.description
						bundle = section.bundle

						action {
							message.reply {
								section.messageBuilder(this)
							}
						}
					}
				}
			}
		}

		if (ephemeral) {
			ephemeralSlashCommand {
				name = "extensions.about.commandName"
				description = "extensions.about.commandDescription"

				if (settings.aboutBuilder.sections.isEmpty()) {
					action {
						respond {
							addAbout(this@action)
						}
					}
				} else {
					ephemeralSubCommand {
						name = "extensions.about.generalCommandName"
						description = "extensions.about.generalCommandDescription"

						action {
							respond {
								addAbout(this@action)
							}
						}
					}

					settings.aboutBuilder.sections.forEach { section ->
						if (section.ephemeral) {
							ephemeralSubCommand {
								name = section.name
								description = section.description
								bundle = section.bundle

								action {
									respond {
										section.messageBuilder(this)
									}
								}
							}
						} else {
							publicSubCommand {
								name = section.name
								description = section.description
								bundle = section.bundle

								action {
									respond {
										section.messageBuilder(this)
									}
								}
							}
						}
					}
				}
			}
		} else {
			publicSlashCommand {
				name = "extensions.about.commandName"
				description = "extensions.about.commandDescription"

				if (settings.aboutBuilder.sections.isEmpty()) {
					action {
						respond {
							addAbout(this@action)
						}
					}
				} else {
					publicSubCommand {
						name = "extensions.about.generalCommandName"
						description = "extensions.about.generalCommandDescription"

						action {
							respond {
								addAbout(this@action)
							}
						}
					}

					settings.aboutBuilder.sections.forEach { section ->
						if (section.ephemeral) {
							ephemeralSubCommand {
								name = section.name
								description = section.description
								bundle = section.bundle

								action {
									respond {
										section.messageBuilder(this)
									}
								}
							}
						} else {
							publicSubCommand {
								name = section.name
								description = section.description
								bundle = section.bundle

								action {
									respond {
										section.messageBuilder(this)
									}
								}
							}
						}
					}
				}
			}
		}
	}

	public suspend fun MessageCreateBuilder.addAbout(context: CommandContext) {
		val builder = settings.aboutBuilder
		val bundle = builder.translationBundle

		embed {
			color = builder.color
			url = builder.url

			builder.fields.forEach {
				field { it() }
			}

			description = buildString {
				if (builder.description != null) {
					append(context.translate(builder.description!!, bundle))
				} else {
					append(context.translate("extensions.about.defaultDescription"))
				}

				if (builder.url != null) {
					append("\n\n")

					append(
						context.translate("extensions.about.descriptionUrl", arrayOf(builder.url))
					)
				}
			}

			title = when {
				builder.name != null && builder.version != null -> context.translate(
					"extensions.about.titleWithVersion",

					arrayOf(
						context.translate(builder.name!!, bundle),
						builder.version!!
					)
				)

				builder.name != null && builder.version == null -> context.translate(
					builder.name!!, bundle
				)

				else -> context.translate("extensions.about.defaultTitle")
			}

			if (builder.logoUrl != null) {
				thumbnail {
					url = builder.logoUrl!!
				}
			}

			footer {
				icon = "https://kordex.dev/logo-transparent.png"
				text = context.translate("extensions.about.madeWith") + " • EUPL v1.2 • https://kordex.dev"
			}
		}

		if (builder.buttons.isNotEmpty()) {
			val names = mutableMapOf<String, String>()

			builder.buttons.forEach { button ->
				names[button.name] = context.translate(button.name, bundle)
			}

			actionRow {
				builder.buttons
					.sortedWith { left, right ->
						names[left.name]!!.compareTo(names[right.name]!!, true)
					}
					.forEach { button ->
						linkButton(button.url) {
							label = names[button.name]!!

							when (val e = button.emoji) {
								null -> {} // Nothing

								is ReactionEmoji.Custom -> emoji(e)
								is ReactionEmoji.Unicode -> emoji(e)
							}
						}
					}
			}
		}
	}
}
