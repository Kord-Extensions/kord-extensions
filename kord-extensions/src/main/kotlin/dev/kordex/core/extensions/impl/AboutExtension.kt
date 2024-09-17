/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.extensions.impl

import dev.kord.core.behavior.UserBehavior
import dev.kord.core.behavior.reply
import dev.kordex.core.DISCORD_BLURPLE
import dev.kordex.core.builders.ExtensibleBotBuilder
import dev.kordex.core.builders.about.Copyright
import dev.kordex.core.builders.about.CopyrightType
import dev.kordex.core.commands.application.slash.ephemeralSubCommand
import dev.kordex.core.commands.application.slash.publicSubCommand
import dev.kordex.core.extensions.Extension
import dev.kordex.core.extensions.chatGroupCommand
import dev.kordex.core.extensions.ephemeralSlashCommand
import dev.kordex.core.extensions.publicSlashCommand
import dev.kordex.core.i18n.generated.CoreTranslations
import dev.kordex.core.pagination.builders.PaginatorBuilder
import org.koin.core.component.inject
import java.util.Locale

@Suppress("StringLiteralDuplication", "MagicNumber")
public class AboutExtension : Extension() {
	override val name: String = "kordex.about"

	private val settings: ExtensibleBotBuilder by inject()

	override suspend fun setup() {
		val ephemeral = settings.aboutBuilder.ephemeral

		chatGroupCommand {
			name = CoreTranslations.Extensions.About.commandName
			description = CoreTranslations.Extensions.About.commandDescription

			chatCommand {
				name = CoreTranslations.Extensions.About.Copyright.commandName
				description = CoreTranslations.Extensions.About.Copyright.commandDescription

				action {
					paginator {
						addCopyright(user, getLocale())
					}.send()
				}
			}

			settings.aboutBuilder.sections.values.forEach { section ->
				this.chatCommand {
					this.name = section.name
					description = section.description

					action {
						message.reply {
							section.builder(this, getLocale())
						}
					}
				}
			}
		}

		if (ephemeral) {
			ephemeralSlashCommand {
				name = CoreTranslations.Extensions.About.commandName
				description = CoreTranslations.Extensions.About.commandDescription

				ephemeralSubCommand {
					name = CoreTranslations.Extensions.About.Copyright.commandName
					description = CoreTranslations.Extensions.About.Copyright.commandDescription

					action {
						val locale = getLocale()

						editingPaginator {
							addCopyright(user, locale)
						}.send()
					}
				}

				settings.aboutBuilder.sections.values.forEach { section ->
					if (section.ephemeral ?: settings.aboutBuilder.ephemeral) {
						ephemeralSubCommand {
							name = section.name
							description = section.description

							action {
								respond {
									section.builder(this, getLocale())
								}
							}
						}
					} else {
						publicSubCommand {
							name = section.name
							description = section.description

							action {
								respond {
									section.builder(this, getLocale())
								}
							}
						}
					}
				}
			}
		} else {
			publicSlashCommand {
				name = CoreTranslations.Extensions.About.commandName
				description = CoreTranslations.Extensions.About.commandDescription

				publicSubCommand {
					name = CoreTranslations.Extensions.About.Copyright.commandName
					description = CoreTranslations.Extensions.About.Copyright.commandDescription

					action {
						val locale = getLocale()

						editingPaginator {
							addCopyright(user, locale)
						}.send()
					}
				}

				settings.aboutBuilder.sections.values.forEach { section ->
					if (section.ephemeral ?: settings.aboutBuilder.ephemeral) {
						ephemeralSubCommand {
							name = section.name
							description = section.description

							action {
								respond {
									section.builder(this, getLocale())
								}
							}
						}
					} else {
						publicSubCommand {
							name = section.name
							description = section.description

							action {
								respond {
									section.builder(this, getLocale())
								}
							}
						}
					}
				}
			}
		}
	}

	public fun PaginatorBuilder.addCopyright(owner: UserBehavior?, locale: Locale) {
		val copyright = settings.aboutBuilder.copyrightItems +
			settings.pluginBuilder.managerObj.plugins.map {
				Copyright(
					"Plugin: `${it.descriptor.pluginId}`",
					it.descriptor.license,
					CopyrightType.PluginModule,
					null
				)
			}

		this.owner = owner

		page {
			color = DISCORD_BLURPLE
			title = "Copyright Information"

			description = CoreTranslations.Extensions.About.Copyright.intro
				.withLocale(locale)
				.translate(
					"[Kord Extensions](https://kordex.dev)",
					"EUPL",
					"1.2"
				)
		}

		copyright
			.groupBy { it.type.key.withLocale(locale).translate() }
			.toSortedMap { left, right -> left.compareTo(right) }
			.forEach { (type, items) ->
				items
					.sortedBy { it.name }
					.chunked(20)
					.forEach { chunk ->
						page {
							color = DISCORD_BLURPLE
							title = type

							description = buildString {
								chunk.forEach { item ->
									if (item.url != null) {
										appendLine(
											"- [${item.name}](${item.url}) (${item.license})"
										)
									} else {
										appendLine(
											"- ${item.name} (${item.license})"
										)
									}
								}
							}
						}
					}
			}
	}
}
