/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

@file:Suppress("StringLiteralDuplication")

package com.kotlindiscord.kord.extensions.modules.extra.mappings

import com.kotlindiscord.kord.extensions.checks.types.Check
import com.kotlindiscord.kord.extensions.checks.types.CheckContext
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.PublicSlashCommandContext
import com.kotlindiscord.kord.extensions.commands.application.slash.publicSubCommand
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.modules.extra.mappings.arguments.*
import com.kotlindiscord.kord.extensions.modules.extra.mappings.builders.ExtMappingsBuilder
import com.kotlindiscord.kord.extensions.modules.extra.mappings.enums.Channels
import com.kotlindiscord.kord.extensions.modules.extra.mappings.exceptions.UnsupportedNamespaceException
import com.kotlindiscord.kord.extensions.modules.extra.mappings.utils.*
import com.kotlindiscord.kord.extensions.pagination.EXPAND_EMOJI
import com.kotlindiscord.kord.extensions.pagination.PublicResponsePaginator
import com.kotlindiscord.kord.extensions.pagination.pages.Page
import com.kotlindiscord.kord.extensions.pagination.pages.Pages
import com.kotlindiscord.kord.extensions.sentry.BreadcrumbType
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.withContext
import me.shedaniel.linkie.*
import me.shedaniel.linkie.namespaces.*
import me.shedaniel.linkie.utils.MappingsQuery
import me.shedaniel.linkie.utils.QueryContext
import me.shedaniel.linkie.utils.QueryResult
import me.shedaniel.linkie.utils.ResultHolder
import mu.KotlinLogging

private typealias MappingSlashCommand = PublicSlashCommandContext<out MappingArguments>
private typealias ConversionSlashCommand = PublicSlashCommandContext<MappingConversionArguments>

private const val VERSION_CHUNK_SIZE = 10
private const val PAGE_FOOTER = "Powered by Linkie"

private const val PAGE_FOOTER_ICON =
    "https://cdn.discordapp.com/attachments/789139884307775580/790887070334976020/linkie_arrow.png"

/**
 * Extension providing Minecraft mappings lookups on Discord.
 */
class MappingsExtension : Extension() {
    private val logger = KotlinLogging.logger { }
    override val name: String = "mappings"

    override suspend fun setup() {
        val namespaces = mutableListOf<Namespace>()
        val enabledNamespaces = builder.config.getEnabledNamespaces()

        enabledNamespaces.forEach {
            when (it) {
                "legacy-yarn" -> namespaces.add(LegacyYarnNamespace)
                "mcp" -> namespaces.add(McpNamespaceReplacement)
                "mojang" -> namespaces.add(MojangNamespace)
                "hashed-mojang" -> namespaces.add(MojangHashedNamespace)
                "plasma" -> namespaces.add(PlasmaNamespace)
                "yarn" -> namespaces.add(YarnNamespace)
                "yarrn" -> namespaces.add(YarrnNamespace)

                else -> throw UnsupportedNamespaceException(it)
            }
        }

        if (namespaces.isEmpty()) {
            logger.warn { "No namespaces have been enabled, not registering commands." }
            return
        }

        Namespaces.init(LinkieConfig.DEFAULT.copy(namespaces = namespaces))

        val legacyYarnEnabled = enabledNamespaces.contains("legacy-yarn")
        val mcpEnabled = enabledNamespaces.contains("mcp")
        val mojangEnabled = enabledNamespaces.contains("mojang")
        val hashedMojangEnabled = enabledNamespaces.contains("hashed-mojang")
        val plasmaEnabled = enabledNamespaces.contains("plasma")
        val yarnEnabled = enabledNamespaces.contains("yarn")
        val yarrnEnabled = enabledNamespaces.contains("yarrn")

        val categoryCheck: Check<ChatInputCommandInteractionCreateEvent> = {
            allowedCategory(builder.config.getAllowedCategories(), builder.config.getBannedCategories())
        }

        val channelCheck: Check<ChatInputCommandInteractionCreateEvent> = {
            allowedGuild(builder.config.getAllowedChannels(), builder.config.getBannedChannels())
        }

        val guildCheck: Check<ChatInputCommandInteractionCreateEvent> = {
            allowedGuild(builder.config.getAllowedGuilds(), builder.config.getBannedGuilds())
        }

        val yarnChannels = Channels.values().joinToString(", ") { "`${it.readableName}`" }

        suspend fun <T : MappingArguments> slashCommand(
            parentName: String,
            friendlyName: String,
            namespace: Namespace,
            arguments: () -> T,
            customInfoCommand: (suspend PublicSlashCommandContext<out Arguments>.() -> Unit)? = null
        ) = publicSlashCommand {
            name = parentName
            description = "Look up $friendlyName mappings."

            publicSubCommand(arguments) {
                name = "class"

                description = "Look up $friendlyName mappings info for a class."

                check { customChecks(name, namespace) }
                check(categoryCheck, channelCheck, guildCheck)

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
                check(categoryCheck, channelCheck, guildCheck)

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
                check(categoryCheck, channelCheck, guildCheck)

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
                check(categoryCheck, channelCheck, guildCheck)

                action(customInfoCommand ?: {
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
                        "$friendlyName mappings are available for queries across **$versionSize** versions.\n\n" +

                            "**Default version:** $defaultVersion\n" +
                            "**Commands:** `/$parentName class`, `/$parentName field`, `/$parentName method`\n\n" +

                            "For a full list of supported $friendlyName versions, please view the rest of the pages."
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
                        timeoutSeconds = getTimeout(),
                        locale = getLocale(),
                        interaction = interactionResponse
                    )

                    paginator.send()
                })
            }
        }

        // region: Legacy Yarn mappings lookups

        if (legacyYarnEnabled) {
            slashCommand(
                "lyarn",
                "Legacy Yarn",
                LegacyYarnNamespace,
                ::LegacyYarnArguments
            )
        }

        // endregion

        // region: MCP mappings lookups

        if (mcpEnabled) {
            // Slash commands
            slashCommand(
                "mcp",
                "MCP",
                McpNamespaceReplacement,
                ::MCPArguments
            )
        }

        // endregion

        // region: Mojang mappings lookups

        if (mojangEnabled) {
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

                        "**Channels:** " + Channels.values().joinToString(", ") { "`${it.readableName}`" } +
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
                    timeoutSeconds = getTimeout(),
                    locale = getLocale(),
                    interaction = interactionResponse
                )

                paginator.send()
            }
        }

        // endregion

        // region: Hashed Mojang mappings lookups

        if (hashedMojangEnabled) {
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

                        "**Channels:** " + Channels.values().joinToString(", ") { "`${it.readableName}`" } +
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
                    timeoutSeconds = getTimeout(),
                    locale = getLocale(),
                    interaction = interactionResponse
                )

                paginator.send()
            }
        }

        // endregion

        // region: Plasma mappings lookups

        if (plasmaEnabled) {
            slashCommand(
                "plasma",
                "Plasma",
                PlasmaNamespace,
                ::PlasmaArguments
            )
        }

        // endregion

        // region: Yarn mappings lookups

        if (yarnEnabled) {
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

                        if (legacyYarnEnabled) {
                            " For Legacy Yarn mappings, please see the `lyarn` command."
                        } else {
                            ""
                        }
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
                    timeoutSeconds = getTimeout(),
                    locale = getLocale(),
                    interaction = interactionResponse
                )

                paginator.send()
            }
        }

        // endregion

        // region: Yarrn mappings lookups

        if (yarrnEnabled) {
            slashCommand(
                "yarrn",
                "Yarrn",
                YarrnNamespace,
                ::YarrnArguments
            )
        }

        // endregion

        // region: Mapping conversions

        publicSlashCommand {
            name = "convert"
            description = "Convert mappings across namespaces"

            publicSubCommand({ MappingConversionArguments(enabledNamespaces.associateBy { it.lowercase() }) }) {
                name = "class"
                description = "Convert a class mapping"

                action {
                    convertMapping(
                        "class",
                        MappingsQuery::queryClasses,
                        classMatchesToPages,
                        enabledNamespaces,
                        obfNameProvider = { it.obfName.preferredName },
                        classNameProvider = { it.obfName.preferredName!! },
                        descProvider = { null }
                    )
                }
            }

            publicSubCommand({ MappingConversionArguments(enabledNamespaces.associateBy { it.lowercase() }) }) {
                name = "field"
                description = "Convert a field mapping"

                action {
                    convertMapping(
                        "field",
                        MappingsQuery::queryFields,
                        fieldMatchesToPages,
                        enabledNamespaces,
                        obfNameProvider = { it.second.obfName.preferredName },
                        classNameProvider = { it.first.obfName.preferredName!! },
                        descProvider = {
                            when {
                                second.obfName.isMerged() -> second.getObfMergedDesc(it)
                                second.obfName.client != null -> second.getObfClientDesc(it)
                                second.obfName.server != null -> second.getObfServerDesc(it)
                                else -> null
                            }
                        }
                    )
                }
            }

            publicSubCommand({ MappingConversionArguments(enabledNamespaces.associateBy { it.lowercase() }) }) {
                name = "method"
                description = "Convert a method mapping"

                action {
                    convertMapping(
                        "method",
                        MappingsQuery::queryMethods,
                        methodMatchesToPages,
                        enabledNamespaces,
                        obfNameProvider = { it.second.obfName.preferredName },
                        classNameProvider = { it.first.obfName.preferredName!! },
                        descProvider = {
                            when {
                                second.obfName.isMerged() -> second.getObfMergedDesc(it)
                                second.obfName.client != null -> second.getObfClientDesc(it)
                                second.obfName.server != null -> second.getObfServerDesc(it)
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
                        timeoutSeconds = getTimeout(),
                        locale = getLocale(),
                        interaction = interactionResponse
                    )

                    paginator.send()
                }
            }
        }

        // endregion

        logger.info { "Mappings extension set up - namespaces: " + enabledNamespaces.joinToString(", ") }
    }

    private suspend fun <A, B> MappingSlashCommand.queryMapping(
        type: String,
        channel: String? = null,
        queryProvider: suspend (QueryContext) -> QueryResult<A, B>,
        pageGenerationMethod: (Namespace, MappingsContainer, QueryResult<A, B>) -> List<Pair<String, String>>
    ) where A : MappingsMetadata, B : List<*> {
    sentry.breadcrumb(BreadcrumbType.Query) {
        message = "Beginning mapping lookup"

        data["type"] = type
        data["channel"] = channel ?: "N/A"
        data["namespace"] = arguments.namespace.id
        data["query"] = arguments.query
        data["version"] = arguments.version?.version ?: "N/A"
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

            provider.injectDefaultVersion(
                arguments.namespace.getProvider(version ?: arguments.namespace.defaultVersion)
            )

            sentry.breadcrumb(BreadcrumbType.Info) {
                message = "Provider resolved, with injected default version"

                data["version"] = provider.version ?: "Unknown"
            }

            val query = arguments.query.replace('.', '/')
            val pages: List<Pair<String, String>>

            sentry.breadcrumb(BreadcrumbType.Info) {
                message = "Attempting to run sanitized query"

                data["query"] = query
            }

            @Suppress("TooGenericExceptionCaught")
            val result = try {
                queryProvider(QueryContext(
                    provider = provider,
                    searchKey = query
                ))
            } catch (e: NullPointerException) {
                respond {
                    content = e.localizedMessage
                }
                return@withContext
            }

            sentry.breadcrumb(BreadcrumbType.Info) {
                message = "Generating pages for results"

                data["resultCount"] = result.value.size
            }

            val container = provider.get()

            pages = pageGenerationMethod(
                arguments.namespace,
                container,
                result,
//                arguments !is IntermediaryMappable || (arguments as IntermediaryMappable).mapDescriptors
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
                timeoutSeconds = getTimeout(),
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
        obfNameProvider: (B) -> String?,
        classNameProvider: (B) -> String,
        descProvider: B.(MappingsContainer) -> String?,
    ) where A : MappingsMetadata, T : List<ResultHolder<B>> {
        sentry.breadcrumb(BreadcrumbType.Query) {
            message = "Beginning mapping conversion"

            data["type"] = type
            data["query"] = arguments.query
            data["inputNamespace"] = arguments.inputNamespace
            data["inputChannel"] = arguments.inputChannel ?: "N/A"
            data["outputNamespace"] = arguments.outputNamespace
            data["outputChannel"] = arguments.outputChannel ?: "N/A"
            data["version"] = arguments.version ?: "N/A"
        }

        newSingleThreadContext("/convert $type: ${arguments.query}").use { context ->
            withContext(context) {
                val inputNamespace = if (arguments.inputNamespace in enabledNamespaces) {
                    if (arguments.inputNamespace == "hashed-mojang") {
                        // hashed-mojang is referred to by Linkie as `hashed_mojang` which breaks everything
                        MojangHashedNamespace
                    } else {
                        Namespaces[arguments.inputNamespace]
                    }
                } else {
                    returnError("Input namespace is not enabled or available")
                    return@withContext
                }

                val outputNamespace = if (arguments.outputNamespace in enabledNamespaces) {
                    if (arguments.outputNamespace == "hashed-mojang") {
                        // hashed-mojang is referred to by Linkie as `hashed_mojang` which breaks everything
                        MojangHashedNamespace
                    } else {
                        Namespaces[arguments.outputNamespace]
                    }
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

                    data["version"] = inputProvider.version ?: "Unknown"
                }

                val query = arguments.query.replace('.', '/')
                val pages: List<String>

                sentry.breadcrumb(BreadcrumbType.Info) {
                    message = "Attempting to run sanitized query"

                    data["query"] = query
                }

                @Suppress("TooGenericExceptionCaught")
                val inputResult = try {
                    queryProvider(QueryContext(
                        provider = inputProvider,
                        searchKey = query
                    ))
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

                        clazz to result
                    } catch (e: NullPointerException) {
                        null // skip
                    }
                }
                    .filterValues { it != null }
                    .mapValues {
                        @Suppress("UNCHECKED_CAST")
                        it.value!! as B
                    }

                sentry.breadcrumb(BreadcrumbType.Info) {
                    message = "Generating pages for results"

                    data["resultCount"] = outputResults.size
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
                    timeoutSeconds = getTimeout(),
                    locale = getLocale(),
                    interaction = interactionResponse
                )

                paginator.send()
            }
        }
    }

    private suspend fun PublicSlashCommandContext<*>.returnError(errorMessage: String) {
        respond {
            content = errorMessage
        }
    }

    private fun Namespace.getDefaultVersion(channel: String?): String? {
        return when (this) {
            is MojangNamespace -> if (channel == "snapshot") {
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

    private suspend fun getTimeout() = builder.config.getTimeout()

    private suspend fun CheckContext<ChatInputCommandInteractionCreateEvent>.customChecks(
        command: String,
        namespace: Namespace
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

    companion object {
        private lateinit var builder: ExtMappingsBuilder

        /** @suppress: Internal function used to pass the configured builder into the extension. **/
        fun configure(builder: ExtMappingsBuilder) {
            this.builder = builder
        }
    }
}
