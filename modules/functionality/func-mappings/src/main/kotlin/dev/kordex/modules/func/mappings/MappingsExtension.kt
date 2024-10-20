/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

@file:Suppress("StringLiteralDuplication", "UnstableApiUsage")

@file:OptIn(DelicateCoroutinesApi::class, ExperimentalCoroutinesApi::class)

package dev.kordex.modules.func.mappings

import dev.kord.common.entity.Permission
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.GuildBehavior
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.rest.builder.message.EmbedBuilder
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
import dev.kordex.core.i18n.EMPTY_KEY
import dev.kordex.core.i18n.capitalizeWords
import dev.kordex.core.i18n.toKey
import dev.kordex.core.i18n.types.Key
import dev.kordex.core.i18n.withContext
import dev.kordex.core.pagination.EXPAND_EMOJI
import dev.kordex.core.pagination.PublicResponsePaginator
import dev.kordex.core.pagination.pages.Page
import dev.kordex.core.pagination.pages.Pages
import dev.kordex.core.sentry.BreadcrumbType
import dev.kordex.core.storage.StorageType
import dev.kordex.core.storage.StorageUnit
import dev.kordex.core.utils.capitalizeWords
import dev.kordex.modules.func.mappings.arguments.*
import dev.kordex.modules.func.mappings.builders.ExtMappingsBuilder
import dev.kordex.modules.func.mappings.enums.Channels
import dev.kordex.modules.func.mappings.i18n.generated.MappingsTranslations
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
import java.util.Locale
import kotlin.collections.set
import kotlin.error
import kotlin.io.path.Path
import kotlin.io.path.createDirectory
import kotlin.io.path.exists

private typealias MappingSlashCommand = PublicSlashCommandContext<out MappingArguments, out ModalForm>
private typealias ConversionSlashCommand = PublicSlashCommandContext<out MappingConversionArguments, out ModalForm>
private typealias InfoCommand = (suspend PublicSlashCommandContext<out Arguments, out ModalForm>.(ModalForm?) -> Unit)?

private const val VERSION_CHUNK_SIZE = 10
private const val PAGE_FOOTER_ICON =
	"https://linkie.shedaniel.dev/apple-touch-icon.png"

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

/**
 * Extension providing Minecraft mappings lookups on Discord.
 */
class MappingsExtension : Extension() {
	private val logger = KotlinLogging.logger { }
	override val name: String = MappingsPlugin.PLUGIN_ID

	private val guildConfig = StorageUnit(
		StorageType.Config,
		"mappings",
		"guild-config",
		MappingsConfig::class
	)

	private val namespaceCache = mutableMapOf<Snowflake, Map<String, String>>()

	init {
		bot.settings.aboutBuilder.addCopyright()
	}

	override suspend fun setup() {
		// Fix issue where Linkie doesn't create its cache directory
		val cacheDirectory = Path("./.linkie-cache")
		if (!cacheDirectory.exists()) {
			cacheDirectory.createDirectory()
		}

		suspend fun <T : MappingArguments> slashCommand(
			parentName: Key,
			friendlyName: String,
			namespace: Namespace,
			arguments: () -> T,
			customInfoCommand: InfoCommand = null,
		) = publicSlashCommand {
			name = parentName
			description = MappingsTranslations.Command.Generated.description
				.withNamedPlaceholders("mappings" to friendlyName)

			publicSubCommand(arguments) {
				name = MappingsTranslations.Command.Generated.Class.name
				description = MappingsTranslations.Command.Generated.Class.description
					.withNamedPlaceholders("mappings" to friendlyName)

				check { customChecks(name, namespace) }

				action {
					val channel = (this.arguments as? MappingWithChannelArguments)?.channel

					queryMapping(
						QueryType.CLASS,
						channel,
						queryProvider = MappingsQuery::queryClasses,
						pageGenerationMethod = classesToPages
					)
				}
			}

			publicSubCommand(arguments) {
				name = MappingsTranslations.Command.Generated.Field.name
				description = MappingsTranslations.Command.Generated.Field.description
					.withNamedPlaceholders("mappings" to friendlyName)

				check { customChecks(name, namespace) }

				action {
					val channel = (this.arguments as? MappingWithChannelArguments)?.channel

					queryMapping(
						QueryType.FIELD,
						channel,
						queryProvider = MappingsQuery::queryFields,
						pageGenerationMethod = ::fieldsToPages
					)
				}
			}

			publicSubCommand(arguments) {
				name = MappingsTranslations.Command.Generated.Method.name
				description = MappingsTranslations.Command.Generated.Method.description
					.withNamedPlaceholders("mappings" to friendlyName)

				check { customChecks(name, namespace) }

				action {
					val channel = (this.arguments as? MappingWithChannelArguments)?.channel

					queryMapping(
						QueryType.METHOD,
						channel,
						queryProvider = MappingsQuery::queryMethods,
						pageGenerationMethod = ::methodsToPages
					)
				}
			}

			publicSubCommand {
				name = MappingsTranslations.Command.Generated.Info.name
				description = MappingsTranslations.Command.Generated.Info.description
					.withNamedPlaceholders("mappings" to friendlyName)

				check { customChecks(name, namespace) }

				action { modal ->
					if (customInfoCommand != null) {
						return@action customInfoCommand(this, modal)
					}

					val locale = getLocale()
					val defaultVersion = namespace.defaultVersion
					val allVersions = namespace.getAllSortedVersions()

					val pages = allVersions.chunked(VERSION_CHUNK_SIZE).map {
						it.joinToString("\n") { version ->
							"- " + if (version == defaultVersion) {
								MappingsTranslations.Response.Mappings.Version.default
									.withLocale(locale)
									.translateNamed("version" to version)
							} else {
								version
							}
						}
					}.toMutableList()

					val versionSize = allVersions.size

					pages.add(
						0,

						MappingsTranslations.Response.Info.generic
							.withLocale(getLocale())
							.translateNamed(
								"mappings" to friendlyName,
								"totalVersions" to versionSize,
								"defaultVersion" to defaultVersion,
								"commandName" to parentName,
								"classCommand" to MappingsTranslations.Command.Generated.Class.name,
								"fieldCommand" to MappingsTranslations.Command.Generated.Field.name,
								"methodCommand" to MappingsTranslations.Command.Generated.Method.name,
							)
					)

					val pagesObj = Pages()
					val pageTitle = MappingsTranslations.Response.Info.title
						.withLocale(locale)
						.translateNamed("mappings" to friendlyName)

					pages.forEach {
						pagesObj.addPage(
							Page {
								description = it
								title = pageTitle

								defaultFooter(locale)
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

		// region: Barn mappings lookups

		slashCommand(
			"barn".toKey(),
			"Barn",
			BarnNamespace,
			::BarnArguments
		)

		// endregion

		// region: Feather mappings lookups

		slashCommand(
			"feather".toKey(),
			"Feather",
			FeatherNamespace,
			::FeatherArguments
		)

		// endregion

		// region: Legacy Yarn mappings lookups

		slashCommand(
			"legacy-yarn".toKey(),
			"Legacy Yarn",
			LegacyYarnNamespace,
			::LegacyYarnArguments
		)

		// endregion

		// region: MCP mappings lookups

		// Slash commands
		slashCommand(
			"mcp".toKey(),
			"MCP",
			MCPNamespace,
			::MCPArguments
		)

		// endregion

		// region: Mojang mappings lookups

		slashCommand(
			"mojang".toKey(),
			"Mojang",
			MojangNamespace,
			::MojangArguments
		) {
			val locale = getLocale()
			val defaultVersion = MojangReleaseContainer.latestRelease
			val allVersions = MojangNamespace.getAllSortedVersions()

			val pages = allVersions.chunked(VERSION_CHUNK_SIZE).map {
				it.joinToString("\n") { version ->
					"- " + if (version == defaultVersion) {
						MappingsTranslations.Response.Mappings.Version.default
							.withLocale(locale)
							.translateNamed("version" to version)
					} else {
						version
					}
				}
			}.toMutableList()

			pages.add(
				0,

				MappingsTranslations.Response.Info.mojang
					.withLocale(getLocale())
					.translateNamed(
						"totalVersions" to allVersions.size,
						"defaultVersion" to defaultVersion,
						"classCommand" to MappingsTranslations.Command.Generated.Class.name,
						"fieldCommand" to MappingsTranslations.Command.Generated.Field.name,
						"methodCommand" to MappingsTranslations.Command.Generated.Method.name,
					)
			)

			val pagesObj = Pages()
			val pageTitle = MappingsTranslations.Response.Info.title
				.withLocale(locale)
				.translateNamed("mappings" to "Mojang")

			pages.forEach {
				pagesObj.addPage(
					Page {
						description = it
						title = pageTitle

						defaultFooter(getLocale())
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
			"hashed".toKey(),
			"Hashed Mojang",
			MojangHashedNamespace,
			::HashedMojangArguments
		) {
			val locale = getLocale()
			val defaultVersion = MojangReleaseContainer.latestRelease
			val allVersions = MojangNamespace.getAllSortedVersions()

			val pages = allVersions.chunked(VERSION_CHUNK_SIZE).map {
				it.joinToString("\n") { version ->
					"- " + if (version == defaultVersion) {
						MappingsTranslations.Response.Mappings.Version.default
							.withLocale(locale)
							.translateNamed("version" to version)
					} else {
						version
					}
				}
			}.toMutableList()

			pages.add(
				0,

				MappingsTranslations.Response.Info.hashed
					.withLocale(getLocale())
					.translateNamed(
						"totalVersions" to allVersions.size,
						"defaultVersion" to defaultVersion,

						"channels" to Channels.entries.joinToString(", ") {
							"`${it.readableName.translateLocale(locale)}`"
						},

						"classCommand" to MappingsTranslations.Command.Generated.Class.name,
						"fieldCommand" to MappingsTranslations.Command.Generated.Field.name,
						"methodCommand" to MappingsTranslations.Command.Generated.Method.name,
					)
			)

			val pagesObj = Pages()
			val pageTitle = MappingsTranslations.Response.Info.title
				.withLocale(locale)
				.translateNamed("mappings" to "Hashed Mojang")

			pages.forEach {
				pagesObj.addPage(
					Page {
						description = it
						title = pageTitle

						defaultFooter(locale)
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
			"plasma".toKey(),
			"Plasma",
			PlasmaNamespace,
			::PlasmaArguments
		)

		// endregion

		// region: Quilt mappings lookups

		slashCommand(
			"quilt".toKey(),
			"Quilt",
			QuiltMappingsNamespace,
			::QuiltArguments
		)

		// endregion

		// region: SRG Mojang mappings lookups

		slashCommand(
			"srg".toKey(),
			"SRG Mojang",
			MojangSrgNamespace,
			::SrgMojangArguments
		)

		// endregion

		// region: Yarn mappings lookups

		slashCommand(
			"yarn".toKey(),
			"Yarn",
			YarnNamespace,
			::YarnArguments
		) {
			val locale = getLocale()
			val defaultVersion = YarnReleaseContainer.latestRelease
			val defaultSnapshotVersion = YarnReleaseContainer.latestSnapshot
			val allVersions = YarnNamespace.getAllSortedVersions()

			val pages = allVersions.chunked(VERSION_CHUNK_SIZE).map {
				it.joinToString("\n") { version ->
					"- " + when (version) {
						defaultVersion ->
							MappingsTranslations.Response.Mappings.Version.default
								.withLocale(locale)
								.translateNamed("version" to version)

						defaultSnapshotVersion ->
							MappingsTranslations.Response.Mappings.Version.defaultSnapshot
								.withLocale(locale)
								.translateNamed("version" to version)

						else -> version
					}
				}
			}.toMutableList()

			pages.add(
				0,

				MappingsTranslations.Response.Info.yarn
					.withLocale(getLocale())
					.translateNamed(
						"totalVersions" to allVersions.size,
						"defaultVersion" to defaultVersion,
						"snapshotVersion" to defaultSnapshotVersion,

						"channels" to Channels.entries.joinToString(", ") {
							"`${it.readableName.translateLocale(locale)}`"
						},

						"classCommand" to MappingsTranslations.Command.Generated.Class.name,
						"fieldCommand" to MappingsTranslations.Command.Generated.Field.name,
						"methodCommand" to MappingsTranslations.Command.Generated.Method.name,
					)
			)

			val pagesObj = Pages()
			val pageTitle = MappingsTranslations.Response.Info.title
				.withLocale(locale)
				.translateNamed("mappings" to "Yarn")

			pages.forEach {
				pagesObj.addPage(
					Page {
						description = it
						title = pageTitle

						defaultFooter(locale)
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
			"yarrn".toKey(),
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
			name = MappingsTranslations.Command.Convert.name
			description = MappingsTranslations.Command.Convert.description

			Namespaces.init(LinkieConfig.DEFAULT.copy(namespaces = namespaces))

			publicSubCommand<MappingConversionArguments>(
				{ MappingConversionArguments(namespaceGetter) }
			) {
				name = MappingsTranslations.Command.Generated.Class.name
				description = MappingsTranslations.Command.Convert.classDescription

				action {
					val config = guild?.config()
					val enabledNamespaces = config?.namespaces ?: namespaceNames.toList()

					convertMapping(
						QueryType.CLASS,
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
				name = MappingsTranslations.Command.Generated.Field.name
				description = MappingsTranslations.Command.Convert.fieldDescription

				action {
					val config = guild?.config()
					val enabledNamespaces = config?.namespaces ?: namespaceNames.toList()

					convertMapping(
						QueryType.FIELD,
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
				name = MappingsTranslations.Command.Generated.Method.name
				description = MappingsTranslations.Command.Convert.methodDescription

				action {
					val config = guild?.config()
					val enabledNamespaces = config?.namespaces ?: namespaceNames.toList()

					convertMapping(
						QueryType.METHOD,
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
				name = MappingsTranslations.Command.Convert.Info.name
				description = MappingsTranslations.Command.Convert.Info.description

				action {
					val locale = getLocale()
					val config = guild?.config()
					val enabledNamespaces = config?.namespaces ?: namespaceNames.toList()
					val namespacesText = MappingsTranslations.Command.Convert.Info.namespaces
						.translateLocale(locale)

					val pages = mutableListOf<String>()

					pages.add(
						MappingsTranslations.Command.Convert.Info.firstPage
							.translateLocale(locale)
					)

					pages.add(
						"**$namespacesText:**\n\n" +
							enabledNamespaces.joinToString("\n") { "- `$it`" }
					)

					val pagesObj = Pages()
					val pageTitle = MappingsTranslations.Command.Convert.Info.title
						.translateLocale(locale)

					pages.forEach {
						pagesObj.addPage(
							Page {
								description = it
								title = pageTitle

								defaultFooter(locale)
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
			name = MappingsTranslations.Command.Mapping.name
			description = MappingsTranslations.Command.Mapping.description

			check { anyGuild() }

			ephemeralSubCommand(::MappingConfigArguments) {
				name = MappingsTranslations.Command.Mapping.Timeout.name
				description = MappingsTranslations.Command.Mapping.Timeout.description

				check { hasPermission(Permission.ManageGuild) }

				action {
					val guild = getGuild()!!
					val config = guild.config()
					val configUnit = guild.configUnit()

					if (arguments.timeout == null) {
						respond {
							MappingsTranslations.Command.Mapping.Timeout.Response.current
								.withLocale(getLocale())
								.translate(config.timeout)
						}

						return@action
					}

					config.timeout = arguments.timeout!!
					configUnit.save(config)

					respond {
						MappingsTranslations.Command.Mapping.Timeout.Response.updated
							.withLocale(getLocale())
							.translate(config.timeout)
					}
				}
			}

			ephemeralSubCommand {
				name = MappingsTranslations.Command.Mapping.Namespace.name
				description = MappingsTranslations.Command.Mapping.Namespace.description

				check { hasPermission(Permission.ManageGuild) }

				action {
					val locale = getLocale()
					val guild = getGuild()!!
					val config = guild.config()
					val configUnit = guild.configUnit()

					var currentNamespaces: MutableList<String>

					respond {
						content = MappingsTranslations.Command.Mapping.Namespace.selectmenu
							.translateLocale(locale)

						components {
							ephemeralStringSelectMenu {
								maximumChoices = availableNamespaces.size
								minimumChoices = 0

								availableNamespaces.forEach {
									option(
										label = it
											.replace("-", " ")
											.capitalizeWords()
											.toKey(),

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
											content = MappingsTranslations.Command.Mapping.Namespace.Selectmenu.cleared
												.translateLocale(locale)
										}
										return@selectMenu
									}

									currentNamespaces = mutableListOf()
									currentNamespaces.addAll(selectedNamespaces)

									config.namespaces = currentNamespaces
									configUnit.save(config)

									respond {
										content = MappingsTranslations.Command.Mapping.Namespace.Selectmenu.updated
											.withLocale(locale)
											.translate(currentNamespaces.joinToString(", "))
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
		type: QueryType,
		channel: Channels? = null,
		queryProvider: suspend (QueryContext) -> QueryResult<A, B>,
		pageGenerationMethod: (Namespace, MappingsContainer, QueryResult<A, B>, Boolean) -> List<Pair<String, String>>,
	) where A : MappingsMetadata, B : List<*> {
		sentry.breadcrumb(BreadcrumbType.Query) {
			message = "Beginning mapping lookup"

			data["mappings.type"] = type.readableName
			data["mappings.channel"] = channel?.name ?: "N/A"
			data["mappings.namespace"] = arguments.namespace.id
			data["mappings.version"] = arguments.version?.version ?: "N/A"

			data["mappings.query::argument"] = arguments.query
		}

		val locale = getLocale()

		newSingleThreadContext("/query ${type.readableName}: ${arguments.query}").use { context ->
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
						content = MappingsTranslations.Command.Mapping.nodefault
							.withLocale(locale)
							.translate(arguments.namespace.id)
					}
					return@withContext
				}

				provider.injectDefaultVersion(
					arguments.namespace.getProvider(defaultVersion)
				)

				sentry.breadcrumb(BreadcrumbType.Info) {
					message = "Provider resolved with injected default version"

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
						content = MappingsTranslations.Response.Query.noResults
							.translateLocale(locale)
					}

					return@withContext
				}

				val pagesObj = Pages(
					MappingsTranslations.Response.Query.expand
						.withLocale(locale)
						.withNamedPlaceholders("emoji" to EXPAND_EMOJI.mention)
				)

				val pageTitle = MappingsTranslations.Response.Query.title
					.withLocale(locale)
					.translateNamed(
						"mappings" to container.name,
						"type" to type.plural,
						"version" to container.version
					)

				val shortPages = mutableListOf<String>()
				val longPages = mutableListOf<String>()

				pages.forEach { (short, long) ->
					shortPages.add(short)
					longPages.add(long)
				}

				shortPages.forEach {
					pagesObj.addPage(
						MappingsTranslations.Response.Query.expand
							.withLocale(locale)
							.withNamedPlaceholders("emoji" to EXPAND_EMOJI.mention),

						Page {
							description = it
							title = pageTitle

							defaultFooter(locale)
						}
					)
				}

				if (shortPages != longPages) {
					longPages.forEach {
						pagesObj.addPage(
							MappingsTranslations.Response.Query.contract
								.withLocale(locale)
								.withNamedPlaceholders("emoji" to EXPAND_EMOJI.mention),

							Page {
								description = it
								title = pageTitle

								defaultFooter(locale)
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
		type: QueryType,
		queryProvider: suspend (QueryContext) -> QueryResult<A, T>,
		pageGenerationMethod: (MappingsContainer, Map<B, B>, Locale) -> List<String>,
		enabledNamespaces: List<String>,
		obfNameProvider: B.() -> String?,
		classNameProvider: B.() -> String,
		descProvider: B.(MappingsContainer) -> String?,
	) where A : MappingsMetadata, T : List<ResultHolder<B>> {
		sentry.breadcrumb(BreadcrumbType.Query) {
			message = "Beginning mapping conversion"

			data["mappings.type"] = type.readableName
			data["mappings.inputNamespace"] = arguments.inputNamespace
			data["mappings.inputChannel"] = arguments.inputChannel?.readableName ?: "N/A"
			data["mappings.outputNamespace"] = arguments.outputNamespace
			data["mappings.outputChannel"] = arguments.outputChannel?.readableName ?: "N/A"
			data["mappings.version"] = arguments.version ?: "N/A"

			data["mappings.query::argument"] = arguments.query
		}

		newSingleThreadContext("/convert ${type.readableName}: ${arguments.query}").use { context ->
			withContext(context) {
				val inputNamespace = if (arguments.inputNamespace in enabledNamespaces) {
					arguments.inputNamespace.toNamespace(this@convertMapping.getLocale())
				} else {
					returnError(MappingsTranslations.Response.Error.inputNamespace)
					return@withContext
				}

				val outputNamespace = if (arguments.outputNamespace in enabledNamespaces) {
					arguments.outputNamespace.toNamespace(this@convertMapping.getLocale())
				} else {
					returnError(MappingsTranslations.Response.Error.outputNamespace)
					return@withContext
				}

				val newestCommonVersion = inputNamespace.getAllSortedVersions().firstOrNull {
					it in outputNamespace.getAllSortedVersions()
				} ?: run {
					returnError(MappingsTranslations.Response.Error.commonVersion)
					return@withContext
				}

				val inputDefault = inputNamespace.getDefaultVersion(arguments.inputChannel)
				val outputDefault = outputNamespace.getDefaultVersion(arguments.outputChannel)

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
						MappingsTranslations.Response.Error.inputNamespaceUnavailable
							.withNamedPlaceholders(
								"version" to version,
								"namespace" to inputNamespace.id
							)
					)

					return@withContext
				}

				val outputContainer = outputProvider.getOrNull() ?: run {
					returnError(
						MappingsTranslations.Response.Error.outputNamespaceUnavailable
							.withNamedPlaceholders(
								"version" to version,
								"namespace" to inputNamespace.id
							)
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
							QueryType.CLASS -> return@mapValues clazz
							QueryType.METHOD -> clazz.methods
							QueryType.FIELD -> clazz.fields
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
					} catch (_: NullPointerException) {
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

				pages = pageGenerationMethod(outputContainer, outputResults, this@convertMapping.getLocale())
				if (pages.isEmpty()) {
					returnError(MappingsTranslations.Response.Query.noResults)
					return@withContext
				}

				val locale = getLocale()
				val pagesObj = Pages(EMPTY_KEY)

				val inputName = inputContainer.name
				val outputName = outputContainer.name

				val versionName = inputProvider.version
					?: outputProvider.version
					?: MappingsTranslations.Response.unknown

				val pageTitle = MappingsTranslations.Response.Conversion.title
					.withLocale(locale)
					.translateNamed(
						"input" to inputName,
						"output" to outputName,
						"type" to type.singular,
						"version" to versionName
					)

				pages.forEach {
					pagesObj.addPage(
						EMPTY_KEY,

						Page {
							description = it
							title = pageTitle

							defaultFooter(locale)
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

	private suspend fun PublicSlashCommandContext<*, *>.returnError(key: Key) {
		respond {
			content = key
				.withContext(this@returnError)
				.translate()
		}
	}

	private fun Namespace.getDefaultVersion(channel: Channels?): String? {
		return when (this) {
			is MojangNamespace, is MojangHashedNamespace -> if (channel == Channels.SNAPSHOT) {
				MojangReleaseContainer.latestSnapshot
			} else {
				MojangReleaseContainer.latestRelease
			}

			is YarnNamespace -> if (channel == Channels.SNAPSHOT) {
				YarnReleaseContainer.latestSnapshot
			} else {
				YarnReleaseContainer.latestRelease
			}

			else -> null
		}
	}

	private suspend fun GuildBehavior.getTimeout() = config().timeout.toLong()

	private suspend fun CheckContextWithCache<ChatInputCommandInteractionCreateEvent>.customChecks(
		command: Key,
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

	private fun EmbedBuilder.defaultFooter(locale: Locale) {
		footer {
			text = MappingsTranslations.Response.poweredByLinkie
				.withLocale(locale)
				.translate()

			icon = PAGE_FOOTER_ICON
		}
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
			name = MappingsTranslations.Argument.Timeout.name
			description = MappingsTranslations.Argument.Timeout.description
			minValue = 60
		}
	}
}
