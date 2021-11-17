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
import com.kotlindiscord.kord.extensions.modules.extra.mappings.enums.YarnChannels
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
import mu.KotlinLogging

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
                "mcp" -> namespaces.add(MCPNamespace)
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

        val patchworkEnabled = builder.config.yarnChannelEnabled(YarnChannels.PATCHWORK)

        val categoryCheck: Check<ChatInputCommandInteractionCreateEvent> = {
            allowedCategory(builder.config.getAllowedCategories(), builder.config.getBannedCategories())
        }

        val channelCheck: Check<ChatInputCommandInteractionCreateEvent> = {
            allowedGuild(builder.config.getAllowedChannels(), builder.config.getBannedChannels())
        }

        val guildCheck: Check<ChatInputCommandInteractionCreateEvent> = {
            allowedGuild(builder.config.getAllowedGuilds(), builder.config.getBannedGuilds())
        }

        val yarnChannels = YarnChannels.values().filter {
            it != YarnChannels.PATCHWORK || patchworkEnabled
        }.joinToString(", ") { "`${it.readableName}`" }

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
                    queryClasses(channel)
                }
            }

            publicSubCommand(arguments) {
                name = "field"

                description = "Look up $friendlyName mappings info for a field."

                check { customChecks(name, namespace) }
                check(categoryCheck, channelCheck, guildCheck)

                action {
                    val channel = (this.arguments as? MappingWithChannelArguments)?.channel?.readableName
                    queryFields(channel)
                }
            }

            publicSubCommand(arguments) {
                name = "method"

                description = "Look up $friendlyName mappings info for a method."

                check { customChecks(name, namespace) }
                check(categoryCheck, channelCheck, guildCheck)

                action {
                    val channel = (this.arguments as? MappingWithChannelArguments)?.channel?.readableName
                    queryMethods(channel)
                }
            }

            publicSubCommand {
                name = "info"

                description = "Get information for $friendlyName mappings."

                check { customChecks(name, namespace) }
                check(categoryCheck, channelCheck, guildCheck)

                action(customInfoCommand ?: {
                    val defaultVersion = namespace.getDefaultVersion()
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
                MCPNamespace,
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
                val defaultVersion = MojangNamespace.getDefaultVersion()
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
                val defaultVersion = MojangNamespace.getDefaultVersion()
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
                { if (patchworkEnabled) YarnWithPatchworkArguments() else YarnWithoutPatchworkArguments() }
            ) {
                val defaultPatchworkVersion = if (patchworkEnabled) {
                    YarnNamespace.getDefaultVersion { YarnChannels.PATCHWORK.readableName }
                } else {
                    ""
                }

                val defaultVersion = YarnNamespace.getDefaultVersion()
                val defaultSnapshotVersion = YarnNamespace.getDefaultVersion { YarnChannels.SNAPSHOT.readableName }
                val allVersions = YarnNamespace.getAllSortedVersions()

                val pages = allVersions.chunked(VERSION_CHUNK_SIZE).map {
                    it.joinToString("\n") { version ->
                        when (version) {
                            defaultVersion -> "**» $version** (Default)"
                            defaultSnapshotVersion -> "**» $version** (Default: Snapshot)"

                            defaultPatchworkVersion -> if (patchworkEnabled) {
                                "**» $version** (Default: Patchwork)"
                            } else {
                                "**»** $version"
                            }

                            else -> "**»** $version"
                        }
                    }
                }.toMutableList()

                pages.add(
                    0,
                    "Yarn mappings are available for queries across **${allVersions.size}** versions.\n\n" +

                        "**Default version:** $defaultVersion\n" +
                        "**Default snapshot version:** $defaultSnapshotVersion\n\n" +

                        if (patchworkEnabled) {
                            "**Default Patchwork version:** $defaultPatchworkVersion\n\n"
                        } else {
                            ""
                        } +

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
                    convertClass(enabledNamespaces)
                }
            }

            publicSubCommand({ MappingConversionArguments(enabledNamespaces.associateBy { it.lowercase() }) }) {
                name = "field"
                description = "Convert a field mapping"

                action {
                    convertField(enabledNamespaces)
                }
            }

            publicSubCommand({ MappingConversionArguments(enabledNamespaces.associateBy { it.lowercase() }) }) {
                name = "method"
                description = "Convert a method mapping"

                action {
                    convertMethod(enabledNamespaces)
                }
            }

            publicSubCommand {
                name = "info"
                description = "Get information about /convert and its subcommands"

                action {
                    val pages = mutableListOf<String>()
                    pages.add(
                        "Mapping conversions are available for any Minecraft version with multiple mapping sets.\n\n" +

                        "**Default version:** the latest version between the two mapping versions\n\n" +

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
                    val pageTitle = "Mappings info: Convert"

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

    private suspend fun PublicSlashCommandContext<out MappingArguments>.queryClasses(
        channel: String? = null
    ) {
        sentry.breadcrumb(BreadcrumbType.Query) {
            message = "Beginning class lookup"

            data["channel"] = channel ?: "N/A"
            data["namespace"] = arguments.namespace.id
            data["query"] = arguments.query
            data["version"] = arguments.version?.version ?: "N/A"
        }

        newSingleThreadContext("/c: ${arguments.query}").use { context ->
            withContext(context) {
                val version = arguments.version?.version
                val provider = if (version != null) {
                    arguments.namespace.getProvider(version)
                } else {
                    channel?.let {
                        arguments.namespace.getProvider(
                            arguments.namespace.getDefaultVersion { it }
                        )
                    } ?: MappingsProvider.empty(arguments.namespace)
                }

                provider.injectDefaultVersion(
                    arguments.namespace.getDefaultProvider {
                        channel ?: arguments.namespace.getDefaultMappingChannel()
                    }
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
                    MappingsQuery.queryClasses(
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

                    data["resultCount"] = result.value.size
                }

                pages = classesToPages(arguments.namespace, result)
                if (pages.isEmpty()) {
                    respond {
                        content = "No results found"
                    }
                    return@withContext
                }

                val meta = provider.get()

                val pagesObj = Pages("${EXPAND_EMOJI.mention} for more")
                val pageTitle = "List of ${meta.name} classes: ${meta.version}"

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

    private suspend fun PublicSlashCommandContext<out MappingArguments>.queryFields(
        channel: String? = null,
    ) {
        sentry.breadcrumb(BreadcrumbType.Query) {
            message = "Beginning field lookup"

            data["channel"] = channel ?: "N/A"
            data["namespace"] = arguments.namespace.id
            data["query"] = arguments.query
            data["version"] = arguments.version?.version ?: "N/A"
        }

        newSingleThreadContext("/f: ${arguments.query}").use { context ->
            withContext(context) {
                val version = arguments.version?.version
                val provider = if (version != null) {
                    arguments.namespace.getProvider(version)
                } else {
                    channel?.let {
                        arguments.namespace.getProvider(
                            arguments.namespace.getDefaultVersion { it }
                        )
                    } ?: MappingsProvider.empty(arguments.namespace)
                }

                provider.injectDefaultVersion(
                    arguments.namespace.getDefaultProvider {
                        channel ?: arguments.namespace.getDefaultMappingChannel()
                    }
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
                    MappingsQuery.queryFields(
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

                    data["resultCount"] = result.value.size
                }

                pages = fieldsToPages(arguments.namespace, provider.get(), result)
                if (pages.isEmpty()) {
                    respond {
                        content = "No results found"
                    }
                    return@withContext
                }

                val meta = provider.get()

                val pagesObj = Pages("${EXPAND_EMOJI.mention} for more")
                val pageTitle = "List of ${meta.name} fields: ${meta.version}"

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

    private suspend fun PublicSlashCommandContext<out MappingArguments>.queryMethods(
        channel: String? = null,
    ) {
        sentry.breadcrumb(BreadcrumbType.Query) {
            message = "Beginning method lookup"

            data["channel"] = channel ?: "N/A"
            data["namespace"] = arguments.namespace.id
            data["query"] = arguments.query
            data["version"] = arguments.version?.version ?: "N/A"
        }

        newSingleThreadContext("/f: ${arguments.query}").use { context ->
            withContext(context) {
                val version = arguments.version?.version
                val provider = if (version != null) {
                    arguments.namespace.getProvider(version)
                } else {
                    channel?.let {
                        arguments.namespace.getProvider(
                            arguments.namespace.getDefaultVersion { it }
                        )
                    } ?: MappingsProvider.empty(arguments.namespace)
                }

                provider.injectDefaultVersion(
                    arguments.namespace.getDefaultProvider {
                        channel ?: arguments.namespace.getDefaultMappingChannel()
                    }
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
                    MappingsQuery.queryMethods(
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

                    data["resultCount"] = result.value.size
                }

                pages = methodsToPages(arguments.namespace, provider.get(), result)
                if (pages.isEmpty()) {
                    respond {
                        content = "No results found"
                    }
                    return@withContext
                }

                val meta = provider.get()

                val pagesObj = Pages("${EXPAND_EMOJI.mention} for more")
                val pageTitle = "List of ${meta.name} methods: ${meta.version}"

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

    private suspend fun PublicSlashCommandContext<MappingConversionArguments>.convertClass(
        enabledNamespaces: List<String>
    ) {
        sentry.breadcrumb(BreadcrumbType.Query) {
            message = "Beginning class mapping conversion"

            data["query"] = arguments.query
            data["inputNamespace"] = arguments.inputNamespace
            data["inputChannel"] = arguments.inputChannel ?: "N/A"
            data["outputNamespace"] = arguments.outputNamespace
            data["outputChannel"] = arguments.outputChannel ?: "N/A"
            data["version"] = arguments.version ?: "N/A"
        }

        newSingleThreadContext("/convertc: ${arguments.query}").use { context ->
            withContext(context) {
                val returnError: suspend (String) -> Unit = { error ->
                    respond {
                        content = error
                    }
                }

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

                val inputDefault = arguments.inputChannel?.let { inputNamespace.getDefaultVersion { it } }
                val outputDefault = arguments.outputChannel?.let { outputNamespace.getDefaultVersion { it } }

                // try the command-provided version first
                val version = arguments.version
                // then try the default version for the output namespace
                    ?: outputDefault?.takeIf { it in inputNamespace.getAllSortedVersions() }
                    // then try the default version for the input namespace
                    ?: inputDefault?.takeIf { it in outputNamespace.getAllSortedVersions() }
                    // and if all else fails, use the newest common version
                    ?: newestCommonVersion

                val inputProvider = inputNamespace.getProvider(version)
                val outputProvider = outputNamespace.getProvider(version)

                inputProvider.injectDefaultVersion(
                    inputNamespace.getDefaultProvider {
                        arguments.inputChannel ?: inputNamespace.getDefaultMappingChannel()
                    }
                )
                outputProvider.injectDefaultVersion(
                    outputNamespace.getDefaultProvider {
                        arguments.outputChannel ?: outputNamespace.getDefaultMappingChannel()
                    }
                )

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
                    MappingsQuery.queryClasses(QueryContext(
                        provider = inputProvider,
                        searchKey = query
                    ))
                } catch (e: NullPointerException) {
                    returnError(e.localizedMessage)
                    return@withContext
                }

                val outputQueries = inputResult.value.map {
                    it to
                        (it.value.obfName.merged
                        ?: it.value.obfName.client
                        ?: it.value.obfName.server)
                }
                    .filter { it.second != null }
                    .associate { it.first to it.second!! }

                @Suppress("TooGenericExceptionCaught")
                val outputResults = outputQueries.mapValues {
                    try {
                        MappingsQuery.queryClasses(
                            QueryContext(
                                provider = outputProvider,
                                searchKey = it.value
                            )
                        ).value
                    } catch (e: NullPointerException) {
                        null
                    }
                }
                    .filterValues { it != null }
                    .map { (inputClass, outputClasses) ->
                        inputClass.value to outputClasses!!
                            .find { it.value.obfName == inputClass.value.obfName }
                            ?.value
                    }
                    .filter { it.second != null }
                    .map { it.first to it.second!! }

                sentry.breadcrumb(BreadcrumbType.Info) {
                    message = "Generating pages for results"

                    data["resultCount"] = outputResults.size
                }

                pages = classMatchesToPages(outputResults)
                if (pages.isEmpty()) {
                    returnError("No results found")
                    return@withContext
                }

                val inputContainer = inputProvider.get()
                val outputContainer = outputProvider.get()

                val pagesObj = Pages("")
                val inputName = inputContainer.name
                val outputName = outputContainer.name
                val versionName = inputProvider.version ?: outputProvider.version ?: "Unknown"
                val pageTitle = "List of $inputName -> $outputName class mappings: $versionName"

                val shortPages = mutableListOf<String>()

                pages.forEach { short ->
                    shortPages.add(short)
                }

                shortPages.forEach {
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

    private suspend fun PublicSlashCommandContext<MappingConversionArguments>.convertField(
        enabledNamespaces: List<String>
    ) {
        sentry.breadcrumb(BreadcrumbType.Query) {
            message = "Beginning field mapping conversion"

            data["query"] = arguments.query
            data["inputNamespace"] = arguments.inputNamespace
            data["inputChannel"] = arguments.inputChannel ?: "N/A"
            data["outputNamespace"] = arguments.outputNamespace
            data["outputChannel"] = arguments.outputChannel ?: "N/A"
            data["version"] = arguments.version ?: "N/A"
        }

        newSingleThreadContext("/convertf: ${arguments.query}").use { context ->
            withContext(context) {
                val returnError: suspend (String) -> Unit = { error ->
                    respond {
                        content = error
                    }
                }

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

                val inputDefault = arguments.inputChannel?.let { inputNamespace.getDefaultVersion { it } }
                val outputDefault = arguments.outputChannel?.let { outputNamespace.getDefaultVersion { it } }

                // try the command-provided version first
                val version = arguments.version
                // then try the default version for the output namespace
                    ?: outputDefault?.takeIf { it in inputNamespace.getAllSortedVersions() }
                    // then try the default version for the input namespace
                    ?: inputDefault?.takeIf { it in outputNamespace.getAllSortedVersions() }
                    // and if all else fails, use the newest common version
                    ?: newestCommonVersion

                val inputProvider = inputNamespace.getProvider(version)
                val outputProvider = outputNamespace.getProvider(version)

                inputProvider.injectDefaultVersion(
                    inputNamespace.getDefaultProvider {
                        arguments.inputChannel ?: inputNamespace.getDefaultMappingChannel()
                    }
                )
                outputProvider.injectDefaultVersion(
                    outputNamespace.getDefaultProvider {
                        arguments.outputChannel ?: outputNamespace.getDefaultMappingChannel()
                    }
                )

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
                    MappingsQuery.queryFields(QueryContext(
                        provider = inputProvider,
                        searchKey = query
                    ))
                } catch (e: NullPointerException) {
                    returnError(e.localizedMessage)
                    return@withContext
                }

                val outputQueries = inputResult.value.map {
                    it.value to
                        (it.value.second.obfName.merged
                            ?: it.value.second.obfName.client
                            ?: it.value.second.obfName.server)
                }
                    .filter { it.second != null }
                    .associate { it.first to it.second!! }

                @Suppress("TooGenericExceptionCaught")
                val outputResults = outputQueries.mapValues {
                    try {
                        val classes = MappingsQuery.queryClasses(
                            QueryContext(
                                provider = outputProvider,
                                searchKey = it.key.first.obfName.let { obf ->
                                    obf.merged ?: obf.client ?: obf.server
                                }!!
                            )
                        )
                        val clazz = classes.value
                            .first { clazz -> clazz.value.obfName == it.key.first.obfName }
                            .value
                        val field = clazz.fields.first { field -> field.obfName.let { obf ->
                                obf.merged ?: obf.client ?: obf.server
                            } == it.value }
                        clazz to field
                    } catch (e: NullPointerException) {
                        null
                    }
                }
                    .filter { it.value != null }
                    .map { it.key to it.value!! }

                sentry.breadcrumb(BreadcrumbType.Info) {
                    message = "Generating pages for results"

                    data["resultCount"] = outputResults.size
                }

                val inputContainer = inputProvider.get()
                val outputContainer = outputProvider.get()

                pages = fieldMatchesToPages(outputContainer, outputResults)
                if (pages.isEmpty()) {
                    returnError("No results found")
                    return@withContext
                }

                val pagesObj = Pages("")
                val inputName = inputContainer.name
                val outputName = outputContainer.name
                val versionName = inputProvider.version ?: outputProvider.version ?: "Unknown"
                val pageTitle = "List of $inputName -> $outputName field mappings: $versionName"

                val shortPages = mutableListOf<String>()

                pages.forEach { short ->
                    shortPages.add(short)
                }

                shortPages.forEach {
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

    private suspend fun PublicSlashCommandContext<MappingConversionArguments>.convertMethod(
        enabledNamespaces: List<String>
    ) {
        sentry.breadcrumb(BreadcrumbType.Query) {
            message = "Beginning method mapping conversion"

            data["query"] = arguments.query
            data["inputNamespace"] = arguments.inputNamespace
            data["inputChannel"] = arguments.inputChannel ?: "N/A"
            data["outputNamespace"] = arguments.outputNamespace
            data["outputChannel"] = arguments.outputChannel ?: "N/A"
            data["version"] = arguments.version ?: "N/A"
        }

        newSingleThreadContext("/convertm: ${arguments.query}").use { context ->
            withContext(context) {
                val returnError: suspend (String) -> Unit = { error ->
                    respond {
                        content = error
                    }
                }

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

                val inputDefault = arguments.inputChannel?.let { inputNamespace.getDefaultVersion { it } }
                val outputDefault = arguments.outputChannel?.let { outputNamespace.getDefaultVersion { it } }

                // try the command-provided version first
                val version = arguments.version
                    // then try the default version for the output namespace
                    ?: outputDefault?.takeIf { it in inputNamespace.getAllSortedVersions() }
                    // then try the default version for the input namespace
                    ?: inputDefault?.takeIf { it in outputNamespace.getAllSortedVersions() }
                    // and if all else fails, use the newest common version
                    ?: newestCommonVersion

                val inputProvider = inputNamespace.getProvider(version)
                val outputProvider = outputNamespace.getProvider(version)

                val inputContainer = inputProvider.getOrNull() ?: run {
                    returnError("Input mapping is not available ($version probably isn't supported)")
                    return@withContext
                }
                val outputContainer = outputProvider.getOrNull() ?: run {
                    returnError("Output mapping is not available ($version probably isn't supported)")
                    return@withContext
                }

                inputProvider.injectDefaultVersion(
                    inputNamespace.getDefaultProvider {
                        arguments.inputChannel ?: inputNamespace.getDefaultMappingChannel()
                    }
                )
                outputProvider.injectDefaultVersion(
                    outputNamespace.getDefaultProvider {
                        arguments.outputChannel ?: outputNamespace.getDefaultMappingChannel()
                    }
                )

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
                    MappingsQuery.queryMethods(QueryContext(
                        provider = inputProvider,
                        searchKey = query
                    ))
                } catch (e: NullPointerException) {
                    returnError(e.localizedMessage)
                    return@withContext
                }

                val outputQueries = inputResult.value.map {
                    it.value to
                        (it.value.second.obfName.merged
                            ?: it.value.second.obfName.client
                            ?: it.value.second.obfName.server)
                }
                    .filter { it.second != null }
                    .associate { it.first to it.second!! }

                @Suppress("TooGenericExceptionCaught")
                val outputResults = outputQueries.mapValues {
                    try {
                        val classes = MappingsQuery.queryClasses(
                            QueryContext(
                                provider = outputProvider,
                                searchKey = it.key.first.obfName.let { obf ->
                                    obf.merged ?: obf.client ?: obf.server
                                }!!
                            )
                        )
                        val clazz = classes.value
                            .first { clazz -> clazz.value.obfName == it.key.first.obfName }
                            .value
                        val possibilities = clazz.methods.filter { method ->
                            method.obfName.let { obf ->
                                obf.merged ?: obf.client ?: obf.server
                            } == it.value
                        }
                        val useMerged = it.key.second.obfName.isMerged()
                        val useClient = it.key.second.obfName.client != null
                        val useServer = it.key.second.obfName.server != null

                        val inputMethodDesc = when {
                            useMerged -> it.key.second.getObfMergedDesc(inputContainer)
                            useClient -> it.key.second.getObfClientDesc(inputContainer)
                            useServer -> it.key.second.getObfServerDesc(inputContainer)
                            else -> throw NullPointerException() // escape try block
                        }

                        val method = possibilities.find { method ->
                            val desc = when {
                                useMerged -> method.getObfMergedDesc(outputContainer)
                                useClient -> method.getObfClientDesc(outputContainer)
                                useServer -> method.getObfServerDesc(outputContainer)
                                else -> throw NullPointerException() // escape try block
                            }
                            desc == inputMethodDesc
                        }!! // also escape try block

                        clazz to method
                    } catch (e: NullPointerException) {
                        null // just skip this one
                    }
                }
                    .filter { it.value != null }
                    .map { it.key to it.value!! }

                sentry.breadcrumb(BreadcrumbType.Info) {
                    message = "Generating pages for results"

                    data["resultCount"] = outputResults.size
                }

                pages = methodMatchesToPages(outputContainer, outputResults)
                if (pages.isEmpty()) {
                    returnError("No results found")
                    return@withContext
                }

                val pagesObj = Pages("")
                val inputName = inputContainer.name
                val outputName = outputContainer.name
                val versionName = inputProvider.version ?: outputProvider.version ?: "Unknown"
                val pageTitle = "List of $inputName -> $outputName method mappings: $versionName"

                val shortPages = mutableListOf<String>()

                pages.forEach { short ->
                    shortPages.add(short)
                }

                shortPages.forEach {
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
