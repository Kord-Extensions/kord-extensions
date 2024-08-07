/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
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
import dev.kordex.core.extensions.Extension
import dev.kordex.core.extensions.chatCommand
import dev.kordex.core.extensions.ephemeralSlashCommand
import org.koin.core.component.inject

public class AboutExtension : Extension() {
	override val name: String = "kordex.about"

	private val settings: ExtensibleBotBuilder by inject()

	override suspend fun setup() {
		chatCommand {
			name = "extensions.about.commandName"
			description = "extensions.about.commandDescription"

			action {
				message.reply {
					addAbout(this@action)
				}
			}
		}

		ephemeralSlashCommand {
			name = "extensions.about.commandName"
			description = "extensions.about.commandDescription"

			action {
				respond {
					addAbout(this@action)
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
			text = context.translate("extensions.about.madeWith") + " • https://kordex.dev"
		}
	}

	if (builder.buttons.isNotEmpty()) {
		actionRow {
			builder.buttons.forEach { button ->
				linkButton(button.url) {
					label = context.translate(button.name, bundle)

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
