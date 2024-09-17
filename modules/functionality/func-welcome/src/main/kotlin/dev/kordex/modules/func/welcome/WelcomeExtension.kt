/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

@file:Suppress("StringLiteralDuplication")

package dev.kordex.modules.func.welcome

import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.channel.asChannelOf
import dev.kord.core.behavior.channel.asChannelOfOrNull
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.entity.Message
import dev.kord.core.entity.channel.GuildMessageChannel
import dev.kord.core.event.guild.GuildCreateEvent
import dev.kord.core.event.interaction.InteractionCreateEvent
import dev.kord.rest.builder.message.create.UserMessageCreateBuilder
import dev.kord.rest.builder.message.embed
import dev.kordex.core.DISCORD_YELLOW
import dev.kordex.core.commands.Arguments
import dev.kordex.core.commands.application.slash.ephemeralSubCommand
import dev.kordex.core.commands.converters.impl.channel
import dev.kordex.core.commands.converters.impl.defaultingBoolean
import dev.kordex.core.commands.converters.impl.string
import dev.kordex.core.extensions.Extension
import dev.kordex.core.extensions.ephemeralSlashCommand
import dev.kordex.core.extensions.event
import dev.kordex.modules.func.welcome.config.WelcomeChannelConfig
import dev.kordex.modules.func.welcome.data.WelcomeChannelData
import dev.kordex.modules.func.welcome.i18n.generated.WelcomeTranslations
import kotlinx.coroutines.flow.toList
import org.koin.core.component.inject

class WelcomeExtension : Extension() {
	override val name: String = "kordex.func-welcome"

	private val config: WelcomeChannelConfig by inject()
	private val data: WelcomeChannelData by inject()

	private val welcomeChannels: MutableMap<Snowflake, WelcomeChannel> = mutableMapOf()

	init {
		bot.settings.aboutBuilder.addCopyright()
	}

	override suspend fun setup() {
		val initialMapping = data.getChannelURLs()

		event<GuildCreateEvent> {
			action {
				for (it in event.guild.channels.toList()) {
					val channel = it.asChannelOfOrNull<GuildMessageChannel>()
						?: continue

					val url = initialMapping[channel.id]
						?: continue

					val welcomeChannel = WelcomeChannel(channel, url)

					welcomeChannels[channel.id] = welcomeChannel

					welcomeChannel.setup()
				}
			}
		}

		event<InteractionCreateEvent> {
			action {
				welcomeChannels[event.interaction.channelId]?.handleInteraction(event)
			}
		}

		ephemeralSlashCommand {
			name = WelcomeTranslations.Command.Base.name
			description = WelcomeTranslations.Command.Base.description

			allowInDms = false

			config.getStaffCommandChecks().forEach(::check)

			ephemeralSlashCommand(::ChannelArgs) {
				name = WelcomeTranslations.Command.Blocks.name
				description = WelcomeTranslations.Command.Blocks.description

				action {
					val welcomeChannel = welcomeChannels[arguments.channel.id]

					if (welcomeChannel == null) {
						respond {
							content = WelcomeTranslations.Responses.Config.missing
								.translateLocale(getLocale(), arguments.channel.mention)
						}

						return@action
					}

					val blocks = welcomeChannel.getBlocks()

					respond {
						content = buildString {
							if (blocks.isEmpty()) {
								append(
									WelcomeTranslations.Responses.Config.noBlocks
										.translateLocale(getLocale())
								)
							} else {
								blocks.forEach {
									appendLine("* ${it.javaClass.simpleName}")
								}
							}
						}
					}
				}
			}

			ephemeralSubCommand(::ChannelArgs) {
				name = WelcomeTranslations.Command.Delete.name
				description = WelcomeTranslations.Command.Delete.description

				action {
					val locale = getLocale()
					val welcomeChannel = welcomeChannels[arguments.channel.id]

					if (welcomeChannel != null) {
						welcomeChannel.shutdown()
						welcomeChannels.remove(arguments.channel.id)

						val deletedUrl = data.removeChannel(arguments.channel.id)

						respond {
							content = WelcomeTranslations.Responses.Config.removed
								.translateLocale(locale, deletedUrl)
						}

						welcomeChannel.log {
							embed {
								title = WelcomeTranslations.Embed.ChannelRemoved.title.translateLocale(locale)
								color = DISCORD_YELLOW

								description = WelcomeTranslations.Embed.ChannelRemoved.description.translateLocale(locale)

								field {
									name = WelcomeTranslations.Fields.channel.translateLocale(locale)
									value = "${welcomeChannel.channel.mention} (" +
										"`${welcomeChannel.channel.id}` / " +
										"`${welcomeChannel.channel.name}`" +
										")"
								}

								field {
									name = WelcomeTranslations.Fields.staffMember.translateLocale(locale)
									value = "${user.mention} (" +
										"`${user.id}` / " +
										"`${user.asUser().tag}`" +
										")"
								}
							}
						}
					} else {
						respond {
							content = WelcomeTranslations.Responses.Config.missing.translateLocale(locale)
						}
					}
				}
			}

			ephemeralSubCommand(WelcomeExtension::ChannelArgs) {
				name = WelcomeTranslations.Command.Get.name
				description = WelcomeTranslations.Command.Get.description

				action {
					val locale = getLocale()
					val url = data.getUrlForChannel(arguments.channel.id)

					respond {
						content = if (url != null) {
							WelcomeTranslations.Responses.Config.get.translateLocale(locale, url)
						} else {
							WelcomeTranslations.Responses.Config.missing.translateLocale(locale)
						}
					}
				}
			}

			ephemeralSubCommand(WelcomeExtension::ChannelRefreshArgs) {
				name = WelcomeTranslations.Command.Refresh.name
				description = WelcomeTranslations.Command.Refresh.description

				action {
					val locale = getLocale()
					val welcomeChannel = welcomeChannels[arguments.channel.id]

					if (welcomeChannel == null) {
						respond {
							content = WelcomeTranslations.Responses.Config.missing.translateLocale(locale)
						}

						return@action
					}

					respond {
						content = WelcomeTranslations.Responses.refreshingNow
							.translateLocale(locale, arguments.channel.mention)
					}
					welcomeChannel.log {
						embed {
							title = WelcomeTranslations.Embed.ChannelRefreshed.title.translateLocale(locale)
							color = DISCORD_YELLOW

							description = if (arguments.clear) {
								WelcomeTranslations.Embed.ChannelRefreshed.Description.clearing
							} else {
								WelcomeTranslations.Embed.ChannelRefreshed.Description.notClearing
							}.translateLocale(locale)

							field {
								name = WelcomeTranslations.Fields.channel.translateLocale(locale)
								value = "${welcomeChannel.channel.mention} (" +
									"`${welcomeChannel.channel.id}` / " +
									"`${welcomeChannel.channel.name}`" +
									")"
							}

							field {
								name = WelcomeTranslations.Fields.staffMember.translateLocale(locale)
								value = "${user.mention} (" +
									"`${user.id}` / " +
									"`${user.asUser().tag}`" +
									")"
							}
						}
					}

					if (arguments.clear) {
						welcomeChannel.clear()
					}

					welcomeChannel.populate()
				}
			}

			ephemeralSubCommand(WelcomeExtension::ChannelCreateArgs) {
				name = WelcomeTranslations.Command.Set.name
				description = WelcomeTranslations.Command.Set.description

				action {
					val locale = getLocale()
					var welcomeChannel = welcomeChannels[arguments.channel.id]

					if (welcomeChannel != null) {
						welcomeChannels.remove(arguments.channel.id)
						welcomeChannel.shutdown()
					}

					welcomeChannel = WelcomeChannel(arguments.channel.asChannelOf(), arguments.url)

					data.setUrlForChannel(arguments.channel.id, arguments.url)
					welcomeChannels[arguments.channel.id] = welcomeChannel

					respond {
						content = if (arguments.clear) {
							WelcomeTranslations.Responses.Config.Set.clearing
						} else {
							WelcomeTranslations.Responses.Config.Set.notClearing
						}.translateLocale(locale, arguments.channel.mention, arguments.url)
					}

					welcomeChannel.log {
						embed {
							title = WelcomeTranslations.Embed.ChannelUpdated.title.translateLocale(locale)
							color = DISCORD_YELLOW

							description = if (arguments.clear) {
								WelcomeTranslations.Embed.ChannelUpdated.Description.clearing
							} else {
								WelcomeTranslations.Embed.ChannelUpdated.Description.notClearing
							}.translateLocale(locale, arguments.url)

							field {
								name = WelcomeTranslations.Fields.channel.translateLocale(locale)
								value = "${welcomeChannel.channel.mention} (" +
									"`${welcomeChannel.channel.id}` / " +
									"`${welcomeChannel.channel.name}`" +
									")"
							}

							field {
								name = WelcomeTranslations.Fields.staffMember.translateLocale(locale)
								value = "${user.mention} (" +
									"`${user.id}` / " +
									"`${user.asUser().tag}`" +
									")"
							}
						}
					}

					if (arguments.clear) {
						welcomeChannel.clear()
					}

					welcomeChannel.setup()
				}
			}
		}
	}

	suspend fun log(channel: GuildMessageChannel, builder: UserMessageCreateBuilder.() -> Unit): Message? =
		config.getLoggingChannel(channel, channel.guild.asGuild())?.createMessage { builder() }

	override suspend fun unload() {
		welcomeChannels.values.forEach { it.shutdown() }
		welcomeChannels.clear()
	}

	internal class ChannelCreateArgs : Arguments() {
		val channel by channel {
			name = WelcomeTranslations.Args.Channel.name
			description = WelcomeTranslations.Args.Channel.description

			validate {
				failIf(WelcomeTranslations.Args.Channel.validationError) {
					val guildChannel = value.asChannelOfOrNull<GuildMessageChannel>()

					guildChannel == null || guildChannel.guildId != context.getGuild()?.id
				}
			}
		}

		val url by string {
			name = WelcomeTranslations.Args.Url.name
			description = WelcomeTranslations.Args.Url.description

			validate {
				failIf(WelcomeTranslations.Args.Url.validationError) {
					value.contains("://").not() ||
						value.startsWith("://")
				}
			}
		}

		val clear by defaultingBoolean {
			name = WelcomeTranslations.Args.Clear.name
			description = WelcomeTranslations.Args.Clear.description
			defaultValue = false
		}
	}

	internal class ChannelRefreshArgs : Arguments() {
		val channel by channel {
			name = WelcomeTranslations.Args.Channel.name
			description = WelcomeTranslations.Args.Channel.description

			validate {
				failIf(WelcomeTranslations.Args.Channel.validationError) {
					val guildChannel = value.asChannelOfOrNull<GuildMessageChannel>()

					guildChannel == null || guildChannel.guildId != context.getGuild()?.id
				}
			}
		}

		val clear by defaultingBoolean {
			name = WelcomeTranslations.Args.Clear.name
			description = WelcomeTranslations.Args.Clear.description
			defaultValue = false
		}
	}

	internal class ChannelArgs : Arguments() {
		val channel by channel {
			name = WelcomeTranslations.Args.Channel.name
			description = WelcomeTranslations.Args.Channel.description

			validate {
				failIf(WelcomeTranslations.Args.Channel.validationError) {
					val guildChannel = value.asChannelOfOrNull<GuildMessageChannel>()

					guildChannel == null || guildChannel.guildId != context.getGuild()?.id
				}
			}
		}
	}
}
