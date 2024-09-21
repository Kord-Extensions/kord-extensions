/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.extensions.impl

import dev.kord.core.event.message.MessageCreateEvent
import dev.kordex.core.builders.ExtensibleBotBuilder
import dev.kordex.core.builders.extensions.HelpExtensionBuilder
import dev.kordex.core.commands.Arguments
import dev.kordex.core.commands.chat.*
import dev.kordex.core.commands.converters.impl.stringList
import dev.kordex.core.extensions.Extension
import dev.kordex.core.extensions.base.HelpProvider
import dev.kordex.core.extensions.chatCommand
import dev.kordex.core.i18n.EMPTY_KEY
import dev.kordex.core.i18n.generated.CoreTranslations
import dev.kordex.core.i18n.toKey
import dev.kordex.core.pagination.BasePaginator
import dev.kordex.core.pagination.MessageButtonPaginator
import dev.kordex.core.pagination.pages.Page
import dev.kordex.core.pagination.pages.Pages
import dev.kordex.core.utils.deleteIgnoringNotFound
import dev.kordex.core.utils.getLocale
import dev.kordex.core.utils.translate
import io.github.oshai.kotlinlogging.KotlinLogging
import org.koin.core.component.inject

private val logger = KotlinLogging.logger {}

/** Number of commands to show per page. */
public const val HELP_PER_PAGE: Int = 4

private val COMMANDS_GROUP = EMPTY_KEY
private val ARGUMENTS_GROUP = "Arguments".toKey()  // TODO: This needs translating

/**
 * Help command extension.
 *
 * This extension provides a `!help` command listing the available commands,
 * along with a `!help <command>` to get more info about a specific command.
 */
@Suppress("StringLiteralDuplication")
public class HelpExtension : HelpProvider, Extension() {
	override val name: String = "kordex.help"

	/** Message command registry. **/
	public val messageCommandsRegistry: ChatCommandRegistry by inject()

	/** Bot settings. **/
	public val botSettings: ExtensibleBotBuilder by inject()

	/** Help extension settings, from the bot builder. **/
	public val settings: HelpExtensionBuilder =
		botSettings.extensionsBuilder.helpExtensionBuilder

	override suspend fun setup() {
		chatCommand(::HelpArguments) {
			name = CoreTranslations.Extensions.Help.commandName
			aliasKey = CoreTranslations.Extensions.Help.commandAliases
			description = CoreTranslations.Extensions.Help.commandDescription

			localeFallback = true

			check(checks = botSettings.extensionsBuilder.helpExtensionBuilder.checkList.toTypedArray())

			action {
				if (arguments.command.isEmpty()) {
					getMainHelpPaginator(this).send()
				} else {
					getCommandHelpPaginator(this, arguments.command).send()
				}
			}
		}
	}

	override suspend fun getMainHelpPaginator(event: MessageCreateEvent, prefix: String): BasePaginator {
		var totalCommands = 0
		val locale = event.getLocale()

		val pages = Pages(COMMANDS_GROUP)
		val commandPages = gatherCommands(event)
			.chunked(HELP_PER_PAGE)
			.map { list ->
				list.map {
					totalCommands += 1

					formatCommandHelp(prefix, event, it)
				}
			}

		for (page in commandPages) {
			pages.addPage(
				COMMANDS_GROUP,

				Page {
					description = page.joinToString("\n\n") { "${it.first}\n${it.second}" }

					title = CoreTranslations.Extensions.Help.Paginator.Title.commands
						.translateLocale(locale)

					footer {
						text = CoreTranslations.Extensions.Help.Paginator.footer
							.translateLocale(locale, totalCommands)
					}

					color = settings.colourGetter(event)
				}
			)

			pages.addPage(
				ARGUMENTS_GROUP,

				Page {
					description = page.joinToString("\n\n") { "${it.first}\n${it.third}" }
					title = CoreTranslations.Extensions.Help.Paginator.Title.arguments
						.translateLocale(locale)

					footer {
						text = CoreTranslations.Extensions.Help.Paginator.footer
							.translateLocale(locale, totalCommands)
					}

					color = settings.colourGetter(event)
				}
			)
		}

		if (totalCommands < 1) {
			// This should never happen in most cases, but it's best to be safe about it

			pages.addPage(
				COMMANDS_GROUP,
				Page {
					color = settings.colourGetter(event)
					description = CoreTranslations.Extensions.Help.Paginator.noCommands
						.translateLocale(locale)

					title = CoreTranslations.Extensions.Help.Paginator.noCommands
						.translateLocale(locale)

					footer {
						text = CoreTranslations.Extensions.Help.Paginator.footer
							.translateLocale(locale, totalCommands)
					}
				}
			)
		}

		return MessageButtonPaginator(
			keepEmbed = settings.deletePaginatorOnTimeout.not(),
			locale = locale,
			owner = event.message.author,
			pages = pages,
			pingInReply = settings.pingInReply,
			targetMessage = event.message,
			timeoutSeconds = settings.paginatorTimeout,
		).onTimeout {
			if (settings.deleteInvocationOnPaginatorTimeout) {
				@Suppress("TooGenericExceptionCaught")
				try {
					event.message.deleteIgnoringNotFound()
				} catch (t: Throwable) {
					logger.warn(t) { "Failed to delete command invocation." }
				}
			}
		}
	}

	override suspend fun getCommandHelpPaginator(
		event: MessageCreateEvent,
		prefix: String,
		args: List<String>,
	): BasePaginator = getCommandHelpPaginator(event, prefix, getCommand(event, args))

	override suspend fun getCommandHelpPaginator(
		context: ChatCommandContext<*>,
		args: List<String>,
	): BasePaginator =
		getCommandHelpPaginator(context, getCommand(context.event, args))

	override suspend fun getCommandHelpPaginator(
		event: MessageCreateEvent,
		prefix: String,
		command: ChatCommand<out Arguments>?,
	): BasePaginator {
		val pages = Pages(COMMANDS_GROUP)
		val locale = event.getLocale()

		if (command == null || !command.runChecks(event, false, mutableMapOf())) {
			pages.addPage(
				COMMANDS_GROUP,

				Page {
					color = settings.colourGetter(event)

					description = CoreTranslations.Extensions.Help.Error.missingCommandDescription
						.translateLocale(locale)

					title = CoreTranslations.Extensions.Help.Error.missingCommandTitle
						.translateLocale(locale)
				}
			)
		} else {
			val (openingLine, desc, arguments) = formatCommandHelp(prefix, event, command, longDescription = true)

			val commandName = when (command) {
				is ChatSubCommand -> command.getFullTranslatedName(locale)
				is ChatGroupCommand -> command.getFullTranslatedName(locale)
				else -> command.getTranslatedName(locale)
			}

			pages.addPage(
				COMMANDS_GROUP,

				Page {
					color = settings.colourGetter(event)
					description = "$openingLine\n$desc\n\n$arguments"

					title = CoreTranslations.Extensions.Help.Paginator.Title.command
						.translateLocale(locale, commandName)
				}
			)
		}

		return MessageButtonPaginator(
			keepEmbed = settings.deletePaginatorOnTimeout.not(),
			locale = locale,
			owner = event.message.author,
			pages = pages,
			pingInReply = settings.pingInReply,
			targetMessage = event.message,
			timeoutSeconds = settings.paginatorTimeout,
		).onTimeout {
			if (settings.deleteInvocationOnPaginatorTimeout) {
				@Suppress("TooGenericExceptionCaught")
				try {
					event.message.deleteIgnoringNotFound()
				} catch (t: Throwable) {
					logger.warn(t) { "Failed to delete command invocation." }
				}
			}
		}
	}

	override suspend fun gatherCommands(event: MessageCreateEvent): List<ChatCommand<out Arguments>> {
		val locale = event.getLocale()

		return messageCommandsRegistry.commands
			.filter { !it.hidden && it.enabled && it.runChecks(event, false, mutableMapOf()) }
			.sortedBy { it.name.translateLocale(locale).lowercase(locale) }
	}

	override suspend fun formatCommandHelp(
		prefix: String,
		event: MessageCreateEvent,
		command: ChatCommand<out Arguments>,
		longDescription: Boolean,
	): Triple<String, String, String> {
		val locale = event.getLocale()
		val defaultLocale = botSettings.i18nBuilder.defaultLocale

		val commandName = when (command) {
			is ChatSubCommand -> command.getFullTranslatedName(locale)
			is ChatGroupCommand -> command.getFullTranslatedName(locale)
			else -> command.getTranslatedName(locale)
		}

		val openingLine = "**$prefix$commandName ${command.getSignature(locale)}**\n"

		val description = buildString {
			if (longDescription) {
				append(
					command.description
						.translateLocale(locale)
				)
			} else {
				append(
					command.description
						.translateLocale(locale)
						.trim()
						.takeWhile { it != '\n' }
				)
			}

			append("\n")

			val aliases: MutableSet<String> = mutableSetOf()

			aliases.addAll(command.getTranslatedAliases(locale))

			if (command.localeFallback && locale != defaultLocale) {
				aliases.add(command.getTranslatedName(defaultLocale))
				aliases.addAll(command.getTranslatedAliases(defaultLocale))

				aliases.remove(command.getTranslatedName(locale))
			}

			if (aliases.isNotEmpty()) {
				append("\n")

				append(
					CoreTranslations.Extensions.Help.CommandDescription.aliases
						.translateLocale(locale)
				)

				append(" ")
				append(
					aliases.sorted().joinToString(", ") {
						"`$it`"
					}
				)
			}

			if (command is ChatGroupCommand) {
				val subCommands = command.commands.filter { it.runChecks(event, false, mutableMapOf()) }

				if (subCommands.isNotEmpty()) {
					append("\n")

					append(
						CoreTranslations.Extensions.Help.CommandDescription.subCommands
							.translateLocale(locale)
					)

					append(" ")
					append(
						subCommands.map { it.getTranslatedName(locale) }.joinToString(", ") {
							"`$it`"
						}
					)
				}
			}

			if (command.requiredPerms.isNotEmpty()) {
				append("\n")

				append(
					CoreTranslations.Extensions.Help.CommandDescription.requiredBotPermissions
						.translateLocale(locale)
				)

				append(" ")
				append(command.requiredPerms.joinToString(", ") { it.translate(locale) })
			}
		}.trim('\n')

		val arguments = buildString {
			append("\n\n")

			if (command.arguments == null) {
				append(
					CoreTranslations.Extensions.Help.CommandDescription.noArguments
						.translateLocale(locale)
				)
			} else {
				@Suppress("TooGenericExceptionCaught")  // Hard to say really
				try {
					val argsObj = command.arguments!!.invoke()

					append(
						argsObj.args.joinToString("\n") {
							buildString {
								append("**»** `${it.displayName}")

								if (it.converter.showTypeInSignature) {
									append(" (")

									append(
										it.converter.signatureType
											.translateLocale(locale)
									)

									append(")")
								}

								append("`: ")
								append(
									it.description
										.translateLocale(locale)
								)
							}
						}
					)
				} catch (t: Throwable) {
					logger.error(t) { "Failed to retrieve argument list for command: $name" }

					append(
						CoreTranslations.Extensions.Help.CommandDescription.Error.argumentList
							.translateLocale(locale)
					)
				}
			}
		}.trim('\n')

		return Triple(openingLine.trim('\n'), description, arguments)
	}

	override suspend fun getCommand(
		event: MessageCreateEvent,
		args: List<String>,
	): ChatCommand<out Arguments>? {
		val firstArg = args.first()
		var command = messageCommandsRegistry.getCommand(firstArg, event)

		if (command?.runChecks(event, false, mutableMapOf()) == false) {
			return null
		}

		args.drop(1).forEach {
			if (command is ChatGroupCommand<out Arguments>) {
				command = if (command.runChecks(event, false, mutableMapOf())) {
					command.getCommand(it, event)
				} else {
					null
				}
			}
		}

		return command
	}

	/** Help command arguments class. **/
	public class HelpArguments : Arguments() {
		/** Command to get help for. **/
		public val command: List<String> by stringList {
			name = "command".toKey()  // TODO: This needs translating
			description = CoreTranslations.Extensions.Help.CommandArguments.command
		}
	}
}
