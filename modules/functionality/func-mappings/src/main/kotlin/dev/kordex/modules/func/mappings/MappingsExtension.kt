/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

@file:Suppress("StringLiteralDuplication", "UnstableApiUsage")

@file:OptIn(DelicateCoroutinesApi::class, ExperimentalCoroutinesApi::class)

package dev.kordex.modules.func.mappings

import dev.kord.common.entity.Permission
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.GuildBehavior
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kordex.core.checks.anyGuild
import dev.kordex.core.checks.hasPermission
import dev.kordex.core.checks.types.CheckContextWithCache
import dev.kordex.core.commands.Arguments
import dev.kordex.core.commands.application.slash.PublicSlashCommandContext
import dev.kordex.core.commands.application.slash.ephemeralSubCommand
import dev.kordex.core.commands.application.slash.publicSubCommand
import dev.kordex.core.commands.converters.impl.optionalInt
import dev.kordex.core.components.components
import dev.kordex.core.components.ephemeralStringSelectMenu
import dev.kordex.core.components.forms.ModalForm
import dev.kordex.core.extensions.Extension
import dev.kordex.core.extensions.ephemeralSlashCommand
import dev.kordex.core.extensions.publicSlashCommand
import dev.kordex.core.pagination.EXPAND_EMOJI
import dev.kordex.core.pagination.PublicResponsePaginator
import dev.kordex.core.pagination.pages.Page
import dev.kordex.core.pagination.pages.Pages
import dev.kordex.core.sentry.BreadcrumbType
import dev.kordex.core.storage.StorageType
import dev.kordex.core.storage.StorageUnit
import dev.kordex.modules.func.mappings.arguments.*
import dev.kordex.modules.func.mappings.builders.ExtMappingsBuilder
import dev.kordex.modules.func.mappings.enums.Channels
import dev.kordex.modules.func.mappings.plugins.MappingsPlugin
import dev.kordex.modules.func.mappings.storage.MappingsConfig
import dev.kordex.modules.func.mappings.utils.*
import dev.kordex.modules.func.mappings.utils.MojangReleaseContainer
import dev.kordex.modules.func.mappings.utils.YarnReleaseContainer
import dev.kordex.modules.func.mappings.utils.toNamespace
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.withContext
import me.shedaniel.linkie.*
import me.shedaniel.linkie.namespaces.*
import me.shedaniel.linkie.utils.*
import kotlin.collections.set
import kotlin.error
import kotlin.io.path.Path
import kotlin.io.path.createDirectory
import kotlin.io.path.exists

private typealias MappingSlashCommand = PublicSlashCommandContext<out MappingArguments, out ModalForm>
private typealias ConversionSlashCommand = PublicSlashCommandContext<out MappingConversionArguments, out ModalForm>
private typealias InfoCommand = (suspend PublicSlashCommandContext<out Arguments, out ModalForm>.(ModalForm?) -> Unit)?

private const val VERSION_CHUNK_SIZE = 10
private const val PAGE_FOOTER = "Powered by Linkie"

private val availableNamespaces =
	listOf(
		"barn",
		"feather",
		"hashed-mojang",
		"legacy-yarn",
		"mcp",
		"mojang",
		"plasma",
		"quilt-mappings",
		"srg-mojang",
		"yarn",
		"yarrn"
	)

private const val PAGE_FOOTER_ICON =
	"https://cdn.discordapp.com/attachments/789139884307775580/790887070334976020/linkie_arrow.png"

/**
 * Extension providing Minecraft mappings lookups on Discord.
 */
class MappingsExtension : Extension() {
	private val logger = KotlinLogging.logger { }
	override val name: String = MappingsPlugin.PLUGIN_ID
	override val bundle: String = "kordex.func-mappings"

	private val guildConfig = StorageUnit(
		StorageType.Config,
		"mappings",
		"guild-config",
		MappingsConfig::class
	)

	private val namespaceCache = mutableMapOf<Snowflake, Map<String, String>>()

	override suspend fun setup() {
		// Fix issue where Linkie doesn't create its cache directory
		val cacheDirectory = Path("./.linkie-cache")
		if (!cacheDirectory.exists()) {
			cacheDirectory.createDirectory()
		}

		val yarnChannels = Channels.entries.joinToString(", ") { "`${it.readableName}`" }

		suspend fun <T : MappingArguments> slashCommand(
			parentName: String,
			friendlyName: String,
			namespace: Namespace,
			arguments: () -> T,
			customInfoCommand: InfoCommand = null,
		) = publicSlashCommand {
			name = parentName
			description = "Look up $friendlyName mappings."

			publicSubCommand(arguments) {
				name = "class"

				description = "Look up $friendlyName mappings info for a class."

				check { customChecks(name, namespace) }

				action {
					val channel = (this.arguments as? MappingWithChannelArguments)?.channel?.readableName

					queryMapping(
						"class",
						channel,
						queryProvider = MappingsQuery::queryClasses,
						pageGenerationMethod = classesToPages
					)
				}
			}

			publicSubCommand(arguments) {
				name = "field"

				description = "Look up $friendlyName mappings info for a field."

				check { customChecks(name, namespace) }

				action {
					val channel = (this.arguments as? MappingWithChannelArguments)?.channel?.readableName

					queryMapping(
						"field",
						channel,
						queryProvider = MappingsQuery::queryFields,
						pageGenerationMethod = ::fieldsToPages
					)
				}
			}

			publicSubCommand(arguments) {
				name = "method"

				description = "Look up $friendlyName mappings info for a method."

				check { customChecks(name, namespace) }

				action {
					val channel = (this.arguments as? MappingWithChannelArguments)?.channel?.readableName

					queryMapping(
						"method",
						channel,
						queryProvider = MappingsQuery::queryMethods,
						pageGenerationMethod = ::methodsToPages
					)
				}
			}

			publicSubCommand {
				name = "info"

				description = "Get information for $friendlyName mappings."

				check { customChecks(name, namespace) }

				action(
					customInfoCommand ?: {
						val defaultVersion = namespace.defaultVersion
						val allVersions = namespace.getAllSortedVersions()

						val pages = allVersions.chunked(VERSION_CHUNK_SIZE).map {
							it.joinToString("\n") { version ->
								if (version == defaultVersion) {
									"**» $version** (Default)"
								} else {
									"**»** $version"
								}
							}
						}.toMutableList()

						val versionSize = allVersions.size
						pages.add(
							0,
							"$friendlyName mappings are available for queries across **$versionSize** " +
								"versions.\n\n" +

								"**Default version:** $defaultVersion\n" +
								"**Commands:** `/$parentName class`, `/$parentName field`, `/$parentName method`\n\n" +

								"For a full list of supported $friendlyName versions, please view the rest of the " +
								"pages."
						)

						val pagesObj = Pages()
						val pageTitle = "Mappings info: $friendlyName"

						pages.forEach {
							pagesObj.addPage(
								Page {
									description = it
									title = pageTitle

									footer {
										text = PAGE_FOOTER
										icon = PAGE_FOOTER_ICON
									}
								}
							)
						}

						val paginator = PublicResponsePaginator(
							pages = pagesObj,
							keepEmbed = true,
							owner = event.interaction.user,
							timeoutSeconds = guild?.getTimeout(),
							locale = getLocale(),
							interaction = interactionResponse
						)

						paginator.send()
					}
				)
			}
		}

		// region: Barn mappings lookups

		slashCommand(
			"barn",
			"Barn",
			BarnNamespace,
			::BarnArguments
		)

		// endregion

		// region: Feather mappings lookups

		slashCommand(
			"feather",
			"Feather",
			FeatherNamespace,
			::FeatherArguments
		)

		// endregion

		// region: Legacy Yarn mappings lookups

		slashCommand(
			"lyarn",
			"Legacy Yarn",
			LegacyYarnNamespace,
			::LegacyYarnArguments
		)

		// endregion

		// region: MCP mappings lookups

		// Slash commands
		slashCommand(
			"mcp",
			"MCP",
			MCPNamespace,
			::MCPArguments
		)

		// endregion

		// region: Mojang mappings lookups

		slashCommand(
			"mojang",
			"Mojang",
			MojangNamespace,
			::MojangArguments
		) {
			val defaultVersion = MojangReleaseContainer.latestRelease
			val allVersions = MojangNamespace.getAllSortedVersions()

			val pages = allVersions.chunked(VERSION_CHUNK_SIZE).map {
				it.joinToString("\n") { version ->
					if (version == defaultVersion) {
						"**» $version** (Default)"
					} else {
						"**»** $version"
					}
				}
			}.toMutableList()

			pages.add(
				0,
				"Mojang mappings are available for queries across **${allVersions.size}** versions.\n\n" +

					"**Default version:** $defaultVersion\n\n" +

					"**Channels:** " + Channels.entries.joinToString(", ") { "`${it.readableName}`" } +
					"\n" +
					"**Commands:** `/mojang class`, `/mojang field`, `/mojang method`\n\n" +

					"For a full list of supported Mojang versions, please view the rest of the pages."
			)

			val pagesObj = Pages()
			val pageTitle = "Mappings info: Mojang"

			pages.forEach {
				pagesObj.addPage(
					Page {
						description = it
						title = pageTitle

						footer {
							text = PAGE_FOOTER
							icon = PAGE_FOOTER_ICON
						}
					}
				)
			}

			val paginator = PublicResponsePaginator(
				pages = pagesObj,
				keepEmbed = true,
				owner = event.interaction.user,
				timeoutSeconds = guild?.getTimeout(),
				locale = getLocale(),
				interaction = interactionResponse
			)

			paginator.send()
		}

		// endregion

		// region: Hashed Mojang mappings lookups

		slashCommand(
			"hashed",
			"Hashed Mojang",
			MojangHashedNamespace,
			::HashedMojangArguments
		) {
			val defaultVersion = MojangReleaseContainer.latestRelease
			val allVersions = MojangNamespace.getAllSortedVersions()

			val pages = allVersions.chunked(VERSION_CHUNK_SIZE).map {
				it.joinToString("\n") { version ->
					if (version == defaultVersion) {
						"**» $version** (Default)"
					} else {
						"**»** $version"
					}
				}
			}.toMutableList()

			pages.add(
				0,
				"Hashed Mojang mappings are available for queries across **${allVersions.size}** versions.\n\n" +

					"**Default version:** $defaultVersion\n\n" +

					"**Channels:** " + Channels.entries.joinToString(", ") { "`${it.readableName}`" } +
					"\n" +
					"**Commands:** `/hashed class`, `/hashed field`, `/hashed method`\n\n" +

					"For a full list of supported hashed Mojang versions, please view the rest of the pages."
			)

			val pagesObj = Pages()
			val pageTitle = "Mappings info: Hashed Mojang"

			pages.forEach {
				pagesObj.addPage(
					Page {
						description = it
						title = pageTitle

						footer {
							text = PAGE_FOOTER
							icon = PAGE_FOOTER_ICON
						}
					}
				)
			}

			val paginator = PublicResponsePaginator(
				pages = pagesObj,
				keepEmbed = true,
				owner = event.interaction.user,
				timeoutSeconds = guild?.getTimeout(),
				locale = getLocale(),
				interaction = interactionResponse
			)

			paginator.send()
		}

		// endregion

		// region: Plasma mappings lookups

		slashCommand(
			"plasma",
			"Plasma",
			PlasmaNamespace,
			::PlasmaArguments
		)

		// endregion

		// region: Quilt mappings lookups

		slashCommand(
			"quilt",
			"Quilt",
			QuiltMappingsNamespace,
			::QuiltArguments
		)

		// endregion

		// region: SRG Mojang mappings lookups

		slashCommand(
			"srg",
			"SRG Mojang",
			MojangSrgNamespace,
			::SrgMojangArguments
		)

		// endregion

		// region: Yarn mappings lookups

		slashCommand(
			"yarn",
			"Yarn",
			YarnNamespace,
			::YarnArguments
		) {
			val defaultVersion = YarnReleaseContainer.latestRelease
			val defaultSnapshotVersion = YarnReleaseContainer.latestSnapshot
			val allVersions = YarnNamespace.getAllSortedVersions()

			val pages = allVersions.chunked(VERSION_CHUNK_SIZE).map {
				it.joinToString("\n") { version ->
					when (version) {
						defaultVersion -> "**» $version** (Default)"
						defaultSnapshotVersion -> "**» $version** (Default: Snapshot)"

						else -> "**»** $version"
					}
				}
			}.toMutableList()

			pages.add(
				0,
				"Yarn mappings are available for queries across **${allVersions.size}** versions.\n\n" +

					"**Default version:** $defaultVersion\n" +
					"**Default snapshot version:** $defaultSnapshotVersion\n\n" +

					"**Channels:** $yarnChannels\n" +
					"**Commands:** `/yarn class`, `/yarn field`, `/yarn method`\n\n" +

					"For a full list of supported Yarn versions, please view the rest of the pages." +

					" For Legacy Yarn mappings, please see the `lyarn` command."
			)

			val pagesObj = Pages()
			val pageTitle = "Mappings info: Yarn"

			pages.forEach {
				pagesObj.addPage(
					Page {
						description = it
						title = pageTitle

						footer {
							text = PAGE_FOOTER
							icon = PAGE_FOOTER_ICON
						}
					}
				)
			}

			val paginator = PublicResponsePaginator(
				pages = pagesObj,
				keepEmbed = true,
				owner = event.interaction.user,
				timeoutSeconds = guild?.getTimeout(),
				locale = getLocale(),
				interaction = interactionResponse
			)

			paginator.send()
		}

		// endregion

		// region: Yarrn mappings lookups

		slashCommand(
			"yarrn",
			"Yarrn",
			YarrnNamespace,
			::YarrnArguments
		)

		// endregion

		// region: Mapping conversions

		val namespaceNames = mutableSetOf<String>()
		kord.guilds.flatMapMerge { it.config().namespaces.toList().asFlow() }.toSet(namespaceNames)

		val namespaces = namespaceNames.map {
			when (it) {
				"barn" -> BarnNamespace
				"feather" -> FeatherNamespace
				"hashed-mojang" -> MojangHashedNamespace
				"legacy-yarn" -> LegacyYarnNamespace
				"mcp" -> MCPNamespace
				"mojang" -> MojangNamespace
				"plasma" -> PlasmaNamespace
				"quilt-mappings" -> QuiltMappingsNamespace
				"srg-mojang" -> MojangSrgNamespace
				"yarn" -> YarnNamespace
				"yarrn" -> YarrnNamespace

				else -> error("Unknown namespace: $it")
			}
		}

		val namespaceGetter: suspend (Snowflake?) -> Map<String, String> = { guildId ->
			if (guildId == null) {
				namespaceNames.associateBy { it.lowercase() }
			} else {
				namespaceCache.getOrPut(guildId) {
					GuildBehavior(guildId, kord).config().namespaces.associateBy { it.lowercase() }
				}
			}
		}

		publicSlashCommand {
			name = "convert"
			description = "Convert mappings across namespaces"

			Namespaces.init(LinkieConfig.DEFAULT.copy(namespaces = namespaces))

			publicSubCommand<MappingConversionArguments>(
				{ MappingConversionArguments(namespaceGetter) }
			) {
				name = "class"
				description = "Convert a class mapping"

				action {
					val config = guild?.config()
					val enabledNamespaces = config?.namespaces ?: namespaceNames.toList()

					convertMapping(
						"class",
						MappingsQuery::queryClasses,
						classMatchesToPages,
						enabledNamespaces,
						obfNameProvider = { obfName.preferredName },
						classNameProvider = { obfName.preferredName!! },
						descProvider = { null }
					)
				}
			}

			publicSubCommand<MappingConversionArguments>(
				{ MappingConversionArguments(namespaceGetter) }
			) {
				name = "field"
				description = "Convert a field mapping"

				action {
					val config = guild?.config()
					val enabledNamespaces = config?.namespaces ?: namespaceNames.toList()

					convertMapping(
						"field",
						MappingsQuery::queryFields,
						fieldMatchesToPages,
						enabledNamespaces,
						obfNameProvider = { member.obfName.preferredName },
						classNameProvider = { owner.obfName.preferredName!! },
						descProvider = {
							when {
								member.obfName.isMerged() -> member.getObfMergedDesc(it)
								member.obfName.client != null -> member.getObfClientDesc(it)
								member.obfName.server != null -> member.getObfServerDesc(it)
								else -> null
							}
						}
					)
				}
			}

			publicSubCommand<MappingConversionArguments>(
				{ MappingConversionArguments(namespaceGetter) }
			) {
				name = "method"
				description = "Convert a method mapping"

				action {
					val config = guild?.config()
					val enabledNamespaces = config?.namespaces ?: namespaceNames.toList()

					convertMapping(
						"method",
						MappingsQuery::queryMethods,
						methodMatchesToPages,
						enabledNamespaces,
						obfNameProvider = { member.obfName.preferredName },
						classNameProvider = { owner.obfName.preferredName!! },
						descProvider = {
							when {
								member.obfName.isMerged() -> member.getObfMergedDesc(it)
								member.obfName.client != null -> member.getObfClientDesc(it)
								member.obfName.server != null -> member.getObfServerDesc(it)
								else -> null
							}
						}
					)
				}
			}

			publicSubCommand {
				name = "info"
				description = "Get information about /convert and its subcommands"

				action {
					val config = guild?.config()
					val enabledNamespaces = config?.namespaces ?: namespaceNames.toList()

					val pages = mutableListOf<String>()
					pages.add(
						"Mapping conversions are available for any Minecraft version with multiple mapping sets.\n\n" +

							"The version of the output is determined in this order:\n" +
							"\u2022 The version specified by the command, \n" +
							"\u2022 The default version of the output mapping set, \n" +
							"\u2022 The default version of the input mapping set, or\n" +
							"\u2022 The latest version supported by both mapping sets.\n\n" +

							"For a list of available mappings, see the next page."
					)
					pages.add(
						enabledNamespaces.joinToString(
							prefix = "**Namespaces:** \n\n`",
							separator = "`\n`",
							postfix = "`"
						)
					)

					val pagesObj = Pages()
					val pageTitle = "Mapping conversion info"

					pages.forEach {
						pagesObj.addPage(
							Page {
								description = it
								title = pageTitle

								footer {
									text = PAGE_FOOTER
									icon = PAGE_FOOTER_ICON
								}
							}
						)
					}

					val paginator = PublicResponsePaginator(
						pages = pagesObj,
						keepEmbed = true,
						owner = event.interaction.user,
						timeoutSeconds = guild?.getTimeout(),
						locale = getLocale(),
						interaction = interactionResponse
					)

					paginator.send()
				}
			}
		}

		ephemeralSlashCommand {
			name = "command.mapping.name"
			description = "command.mapping.description"

			check { anyGuild() }

			ephemeralSubCommand(::MappingConfigArguments) {
				name = "command.mapping.timeout.name"
				description = "command.mapping.timeout.description"

				check { hasPermission(Permission.ManageGuild) }

				action {
					val guild = getGuild()!!
					val config = guild.config()
					val configUnit = guild.configUnit()

					if (arguments.timeout == null) {
						respond {
							content = translate(
								"command.mapping.timeout.response.current",
								arrayOf(config.timeout.toString())
							)
						}

						return@action
					}

					config.timeout = arguments.timeout!!
					configUnit.save(config)

					respond {
						content = translate(
							"command.mapping.timeout.response.updated",
							arrayOf(config.timeout.toString())
						)
					}
				}
			}

			ephemeralSubCommand {
				name = "command.mapping.namespace.name"
				description = "command.mapping.namespace.description"

				check { hasPermission(Permission.ManageGuild) }

				action {
					val guild = getGuild()!!
					val config = guild.config()
					val configUnit = guild.configUnit()

					var currentNamespaces: MutableList<String>

					val context = this

					respond {
						content = translate(
							"command.mapping.namespace.selectmenu"
						)

						components {
							ephemeralStringSelectMenu {
								maximumChoices = availableNamespaces.size
								minimumChoices = 0

								availableNamespaces.forEach {
									option(
										label = it,
										value = it
									) {
										default = it in config.namespaces
									}
								}

								action selectMenu@{
									val selectedNamespaces = event.interaction.values.toList().map { it }

									if (event.interaction.values.isEmpty()) {
										config.namespaces = listOf()
										configUnit.save(config)
										respond {
											content = context.translate(
												"command.mapping.namespace.selectmenu.cleared",
											)
										}
										return@selectMenu
									}

									currentNamespaces = mutableListOf()
									currentNamespaces.addAll(selectedNamespaces)

									config.namespaces = currentNamespaces
									configUnit.save(config)

									respond {
										content = context.translate(
											"command.mapping.namespace.selectmenu.updated",
											replacements = arrayOf(currentNamespaces.joinToString(", "))
										)
									}
								}
							}
						}
					}
				}
			}
		}

		// endregion

		logger.info { "Mappings extension set up - namespaces: " + namespaceNames.joinToString(", ") }
	}

	private suspend fun <A, B> MappingSlashCommand.queryMapping(
		type: String,
		channel: String? = null,
		queryProvider: suspend (QueryContext) -> QueryResult<A, B>,
		pageGenerationMethod: (Namespace, MappingsContainer, QueryResult<A, B>, Boolean) -> List<Pair<String, String>>,
	) where A : MappingsMetadata, B : List<*> {
		sentry.breadcrumb(BreadcrumbType.Query) {
			message = "Beginning mapping lookup"

			data["mappings.type"] = type
			data["mappings.channel"] = channel ?: "N/A"
			data["mappings.namespace"] = arguments.namespace.id
			data["mappings.version"] = arguments.version?.version ?: "N/A"

			data["mappings.query::argument"] = arguments.query
		}

		newSingleThreadContext("/query $type: ${arguments.query}").use { context ->
			withContext(context) {
				val version = arguments.version?.version
					?: arguments.namespace.getDefaultVersion(channel)

				val provider = if (version != null) {
					arguments.namespace.getProvider(version)
				} else {
					MappingsProvider.empty(arguments.namespace)
				}

				val defaultVersion = version
					?: arguments.namespace.defaultVersion

				if (defaultVersion == null) {
					respond {
						content = translate("command.mapping.nodefault", arguments.namespace.id)
					}
					return@withContext
				}

				provider.injectDefaultVersion(
					arguments.namespace.getProvider(defaultVersion)
				)

				sentry.breadcrumb(BreadcrumbType.Info) {
					message = "Provider resolved, with injected default version"

					data["mappings.version"] = provider.version ?: "Unknown"
				}

				val query = arguments.query.replace('.', '/')
				val pages: List<Pair<String, String>>

				sentry.breadcrumb(BreadcrumbType.Info) {
					message = "Attempting to run sanitized query"

					data["mappings.query::argument"] = query
				}

				@Suppress("TooGenericExceptionCaught")
				val result = try {
					queryProvider(
						QueryContext(
							provider = provider,
							searchKey = query
						)
					)
				} catch (e: NullPointerException) {
					respond {
						content = e.localizedMessage
					}
					return@withContext
				}

				sentry.breadcrumb(BreadcrumbType.Info) {
					message = "Generating pages for results"

					data["results.count"] = result.value.size
				}

				val container = provider.get()

				pages = pageGenerationMethod(
					arguments.namespace,
					container,
					result,
					arguments !is IntermediaryMappable || (arguments as IntermediaryMappable).mapDescriptors
				)

				if (pages.isEmpty()) {
					respond {
						content = "No results found"
					}

					return@withContext
				}

				val pagesObj = Pages("${EXPAND_EMOJI.mention} for more")

				val plural = if (type == "class") "es" else "s"
				val pageTitle = "List of ${container.name} $type$plural: ${container.version}"

				val shortPages = mutableListOf<String>()
				val longPages = mutableListOf<String>()

				pages.forEach { (short, long) ->
					shortPages.add(short)
					longPages.add(long)
				}

				shortPages.forEach {
					pagesObj.addPage(
						"${EXPAND_EMOJI.mention} for more",

						Page {
							description = it
							title = pageTitle

							footer {
								text = PAGE_FOOTER
								icon = PAGE_FOOTER_ICON
							}
						}
					)
				}

				if (shortPages != longPages) {
					longPages.forEach {
						pagesObj.addPage(
							"${EXPAND_EMOJI.mention} for less",

							Page {
								description = it
								title = pageTitle

								footer {
									text = PAGE_FOOTER
									icon = PAGE_FOOTER_ICON
								}
							}
						)
					}
				}

				sentry.breadcrumb(BreadcrumbType.Info) {
					message = "Creating and sending paginator to Discord"
				}

				val paginator = PublicResponsePaginator(
					pages = pagesObj,
					owner = event.interaction.user,
					timeoutSeconds = guild?.getTimeout(),
					locale = getLocale(),
					interaction = interactionResponse
				)

				paginator.send()
			}
		}
	}

	private suspend fun <A, B, T> ConversionSlashCommand.convertMapping(
		type: String,
		queryProvider: suspend (QueryContext) -> QueryResult<A, T>,
		pageGenerationMethod: (MappingsContainer, Map<B, B>) -> List<String>,
		enabledNamespaces: List<String>,
		obfNameProvider: B.() -> String?,
		classNameProvider: B.() -> String,
		descProvider: B.(MappingsContainer) -> String?,
	) where A : MappingsMetadata, T : List<ResultHolder<B>> {
		sentry.breadcrumb(BreadcrumbType.Query) {
			message = "Beginning mapping conversion"

			data["mappings.type"] = type
			data["mappings.inputNamespace"] = arguments.inputNamespace
			data["mappings.inputChannel"] = arguments.inputChannel?.readableName ?: "N/A"
			data["mappings.outputNamespace"] = arguments.outputNamespace
			data["mappings.outputChannel"] = arguments.outputChannel?.readableName ?: "N/A"
			data["mappings.version"] = arguments.version ?: "N/A"

			data["mappings.query::argument"] = arguments.query
		}

		newSingleThreadContext("/convert $type: ${arguments.query}").use { context ->
			withContext(context) {
				val inputNamespace = if (arguments.inputNamespace in enabledNamespaces) {
					arguments.inputNamespace.toNamespace()
				} else {
					returnError("Input namespace is not enabled or available")
					return@withContext
				}

				val outputNamespace = if (arguments.outputNamespace in enabledNamespaces) {
					arguments.outputNamespace.toNamespace()
				} else {
					returnError("Output namespace is not enabled or available")
					return@withContext
				}

				val newestCommonVersion = inputNamespace.getAllSortedVersions().firstOrNull {
					it in outputNamespace.getAllSortedVersions()
				} ?: run {
					returnError("No common version between input and output mappings")
					return@withContext
				}

				val inputDefault = inputNamespace.getDefaultVersion(arguments.inputChannel?.readableName)

				val outputDefault = outputNamespace.getDefaultVersion(arguments.outputChannel?.readableName)

				// try the command-provided version first
				val version = arguments.version
				// then try the default version for the output namespace
					?: outputDefault.takeIf { it in inputNamespace.getAllSortedVersions() }
					// then try the default version for the input namespace
					?: inputDefault.takeIf { it in outputNamespace.getAllSortedVersions() }
					// and if all else fails, use the newest common version
					?: newestCommonVersion

				val inputProvider = inputNamespace.getProvider(version)
				val outputProvider = outputNamespace.getProvider(version)

				val inputContainer = inputProvider.getOrNull() ?: run {
					returnError(
						"Input mapping is not available ($version probably isn't supported by ${inputNamespace.id})"
					)
					return@withContext
				}

				val outputContainer = outputProvider.getOrNull() ?: run {
					returnError(
						"Output mapping is not available ($version probably isn't supported by ${outputNamespace.id})"
					)
					return@withContext
				}

				sentry.breadcrumb(BreadcrumbType.Info) {
					message = "Providers and namespaces resolved"

					data["mappings.version"] = inputProvider.version ?: "Unknown"
				}

				val query = arguments.query.replace('.', '/')
				val pages: List<String>

				sentry.breadcrumb(BreadcrumbType.Info) {
					message = "Attempting to run sanitized query"

					data["mappings.query::argument"] = query
				}

				@Suppress("TooGenericExceptionCaught")
				val inputResult = try {
					queryProvider(
						QueryContext(
							provider = inputProvider,
							searchKey = query
						)
					)
				} catch (e: NullPointerException) {
					returnError(e.localizedMessage)
					return@withContext
				}

				val outputQueries = inputResult.value.map {
					it.value to obfNameProvider(it.value)
				}
					.filter { it.second != null }
					.associate { it.first to it.second!! }

				@Suppress("TooGenericExceptionCaught")
				val outputResults = outputQueries.mapValues {
					try {
						val classes = MappingsQuery.queryClasses(
							QueryContext(
								provider = outputProvider,
								searchKey = classNameProvider(it.key)
							)
						)

						val clazz = classes.value.first { clazz ->
							clazz.value.obfName.preferredName!! == classNameProvider(it.key)
						}
							.value

						val possibilities = when (type) {
							"class" -> return@mapValues clazz
							"method" -> clazz.methods
							"field" -> clazz.fields
							else -> error("`$type` isn't `class`, `field`, or `method`?????")
						}
							.filter { mapping ->
								mapping.obfName.preferredName == it.value
							}

						// NPE escapes the try block so it's ok
						val inputDesc = it.key.descProvider(inputContainer)!!

						val result = possibilities.find { member ->
							val desc = runCatching { member.getObfMergedDesc(outputContainer) }
								.recoverCatching { member.getObfClientDesc(outputContainer) }
								.recoverCatching { member.getObfServerDesc(outputContainer) }
								.getOrElse {
									return@find false
								}

							desc == inputDesc
						}!!

						MemberEntry<MappingsMember>(clazz, result)
					} catch (e: NullPointerException) {
						null // skip
					}
				}
					.filterValues { it != null }
					.mapValues { (_, value) ->
						@Suppress("UNCHECKED_CAST")
						value as B
					}

				sentry.breadcrumb(BreadcrumbType.Info) {
					message = "Generating pages for results"

					data["results.count"] = outputResults.size
				}

				pages = pageGenerationMethod(outputContainer, outputResults)
				if (pages.isEmpty()) {
					returnError("No results found")
					return@withContext
				}

				val pagesObj = Pages("")

				val inputName = inputContainer.name
				val outputName = outputContainer.name

				val versionName = inputProvider.version ?: outputProvider.version ?: "Unknown"

				val pageTitle = "List of $inputName -> $outputName $type mappings: $versionName"

				pages.forEach {
					pagesObj.addPage(
						"",

						Page {
							description = it
							title = pageTitle

							footer {
								text = PAGE_FOOTER
								icon = PAGE_FOOTER_ICON
							}
						}
					)
				}

				sentry.breadcrumb(BreadcrumbType.Info) {
					message = "Creating and sending paginator to Discord"
				}

				val paginator = PublicResponsePaginator(
					pages = pagesObj,
					owner = event.interaction.user,
					timeoutSeconds = guild?.getTimeout(),
					locale = getLocale(),
					interaction = interactionResponse
				)

				paginator.send()
			}
		}
	}

	private suspend fun PublicSlashCommandContext<*, *>.returnError(errorMessage: String) {
		respond {
			content = errorMessage
		}
	}

	private fun Namespace.getDefaultVersion(channel: String?): String? {
		return when (this) {
			is MojangNamespace, is MojangHashedNamespace -> if (channel == "snapshot") {
				MojangReleaseContainer.latestSnapshot
			} else {
				MojangReleaseContainer.latestRelease
			}

			is YarnNamespace -> if (channel == "snapshot") {
				YarnReleaseContainer.latestSnapshot
			} else {
				YarnReleaseContainer.latestRelease
			}

			else -> null
		}
	}

	private suspend fun GuildBehavior.getTimeout() = config().timeout.toLong()

	private suspend fun CheckContextWithCache<ChatInputCommandInteractionCreateEvent>.customChecks(
		command: String,
		namespace: Namespace,
	) {
		builder.commandChecks.forEach {
			it(command)()

			if (!passed) {
				return
			}
		}

		builder.namespaceChecks.forEach {
			it(namespace)()

			if (!passed) {
				return
			}
		}
	}

	private fun GuildBehavior.configUnit() =
		guildConfig.withGuild(id)

	private suspend fun GuildBehavior.config(): MappingsConfig {
		val config = configUnit()

		return config.get()
			?: config.save(MappingsConfig())
	}

	companion object {
		private lateinit var builder: ExtMappingsBuilder

		/** @suppress: Internal function used to pass the configured builder into the extension. **/
		fun configure(builder: ExtMappingsBuilder) {
			this.builder = builder
		}
	}

	@Suppress("MagicNumber")
	inner class MappingConfigArguments : Arguments() {
		val timeout by optionalInt {
			name = "argument.timeout.name"
			description = "argument.timeout.description"
			minValue = 60
		}
	}
}
