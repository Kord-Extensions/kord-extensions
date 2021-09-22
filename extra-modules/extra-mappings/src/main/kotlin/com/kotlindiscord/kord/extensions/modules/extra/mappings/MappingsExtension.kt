@file:Suppress("StringLiteralDuplication")

package com.kotlindiscord.kord.extensions.modules.extra.mappings

import com.kotlindiscord.kord.extensions.checks.types.Check
import com.kotlindiscord.kord.extensions.checks.types.CheckContext
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.chat.ChatCommandContext
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.chatCommand
import com.kotlindiscord.kord.extensions.modules.extra.mappings.arguments.*
import com.kotlindiscord.kord.extensions.modules.extra.mappings.builders.ExtMappingsBuilder
import com.kotlindiscord.kord.extensions.modules.extra.mappings.enums.Channels
import com.kotlindiscord.kord.extensions.modules.extra.mappings.enums.YarnChannels
import com.kotlindiscord.kord.extensions.modules.extra.mappings.exceptions.UnsupportedNamespaceException
import com.kotlindiscord.kord.extensions.modules.extra.mappings.utils.classesToPages
import com.kotlindiscord.kord.extensions.modules.extra.mappings.utils.fieldsToPages
import com.kotlindiscord.kord.extensions.modules.extra.mappings.utils.methodsToPages
import com.kotlindiscord.kord.extensions.pagination.EXPAND_EMOJI
import com.kotlindiscord.kord.extensions.pagination.MessageButtonPaginator
import com.kotlindiscord.kord.extensions.pagination.pages.Page
import com.kotlindiscord.kord.extensions.pagination.pages.Pages
import com.kotlindiscord.kord.extensions.sentry.BreadcrumbType
import com.kotlindiscord.kord.extensions.utils.respond
import dev.kord.core.behavior.channel.withTyping
import dev.kord.core.event.message.MessageCreateEvent
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

        val categoryCheck: Check<MessageCreateEvent> = {
            allowedCategory(builder.config.getAllowedCategories(), builder.config.getBannedCategories())
        }

        val channelCheck: Check<MessageCreateEvent> = {
            allowedGuild(builder.config.getAllowedChannels(), builder.config.getBannedChannels())
        }

        val guildCheck: Check<MessageCreateEvent> = {
            allowedGuild(builder.config.getAllowedGuilds(), builder.config.getBannedGuilds())
        }

        val yarnChannels = YarnChannels.values().filter {
            it != YarnChannels.PATCHWORK || patchworkEnabled
        }.joinToString(", ") { "`${it.str}`" }

        // region: Legacy Yarn mappings lookups

        if (legacyYarnEnabled) {
            // Class
            chatCommand(::LegacyYarnArguments) {
                name = "lyc"
                aliases = arrayOf("lyarnc", "legacy-yarnc", "legacyyarnc", "legacyarnc")

                description = "Look up Legacy Yarn mappings info for a class.\n\n" +

                    "For more information or a list of versions for Legacy Yarn mappings, you can use the " +
                    "`lyarn` command."

                check { customChecks(name, LegacyYarnNamespace) }
                check(categoryCheck, channelCheck, guildCheck)  // Default checks

                action {
                    queryClasses(LegacyYarnNamespace, arguments.query, arguments.version)
                }
            }

            // Field
            chatCommand(::LegacyYarnArguments) {
                name = "lyf"
                aliases = arrayOf("lyarnf", "legacy-yarnf", "legacyyarnf", "legacyarnf")

                description = "Look up Legacy Yarn mappings info for a field.\n\n" +

                    "For more information or a list of versions for Legacy Yarn mappings, you can use the " +
                    "`lyarn` command."

                check { customChecks(name, LegacyYarnNamespace) }
                check(categoryCheck, channelCheck, guildCheck)  // Default checks

                action {
                    queryFields(LegacyYarnNamespace, arguments.query, arguments.version)
                }
            }

            // Method
            chatCommand(::LegacyYarnArguments) {
                name = "lym"
                aliases = arrayOf("lyarnm", "legacy-yarnm", "legacyyarnm", "legacyarnm")

                description = "Look up Legacy Yarn mappings info for a method.\n\n" +

                    "For more information or a list of versions for Legacy Yarn mappings, you can use the " +
                    "`lyarn` command."

                check { customChecks(name, LegacyYarnNamespace) }
                check(categoryCheck, channelCheck, guildCheck)  // Default checks

                action {
                    queryMethods(LegacyYarnNamespace, arguments.query, arguments.version)
                }
            }
        }

        // endregion

        // region: MCP mappings lookups

        if (mcpEnabled) {
            // Class
            chatCommand(::MCPArguments) {
                name = "mcpc"

                description = "Look up MCP mappings info for a class.\n\n" +

                    "For more information or a list of versions for MCP mappings, you can use the `mcp` command."

                check { customChecks(name, MCPNamespace) }
                check(categoryCheck, channelCheck, guildCheck)  // Default checks

                action {
                    queryClasses(MCPNamespace, arguments.query, arguments.version)
                }
            }

            // Field
            chatCommand(::MCPArguments) {
                name = "mcpf"

                description = "Look up MCP mappings info for a field.\n\n" +

                    "For more information or a list of versions for MCP mappings, you can use the `mcp` command."

                check { customChecks(name, MCPNamespace) }
                check(categoryCheck, channelCheck, guildCheck)  // Default checks

                action {
                    queryFields(MCPNamespace, arguments.query, arguments.version)
                }
            }

            // Method
            chatCommand(::MCPArguments) {
                name = "mcpm"

                description = "Look up MCP mappings info for a method.\n\n" +

                    "For more information or a list of versions for MCP mappings, you can use the `mcp` command."

                check { customChecks(name, MCPNamespace) }
                check(categoryCheck, channelCheck, guildCheck)  // Default checks

                action {
                    queryMethods(MCPNamespace, arguments.query, arguments.version)
                }
            }
        }

        // endregion

        // region: Mojang mappings lookups

        if (mojangEnabled) {
            // Class
            chatCommand(::MojangArguments) {
                name = "mmc"
                aliases = arrayOf("mojc", "mojmapc")

                description = "Look up Mojang mappings info for a class.\n\n" +

                    "**Channels:** " + Channels.values().joinToString(", ") { "`${it.str}`" } +
                    "\n\n" +

                    "For more information or a list of versions for Mojang mappings, you can use the `mojang` " +
                    "command."

                check { customChecks(name, MojangNamespace) }
                check(categoryCheck, channelCheck, guildCheck)  // Default checks

                action {
                    queryClasses(MojangNamespace, arguments.query, arguments.version, arguments.channel?.str)
                }
            }

            // Field
            chatCommand(::MojangArguments) {
                name = "mmf"
                aliases = arrayOf("mojf", "mojmapf")

                description = "Look up Mojang mappings info for a field.\n\n" +

                    "**Channels:** " + Channels.values().joinToString(", ") { "`${it.str}`" } +
                    "\n\n" +

                    "For more information or a list of versions for Mojang mappings, you can use the `mojang` " +
                    "command."

                check { customChecks(name, MojangNamespace) }
                check(categoryCheck, channelCheck, guildCheck)  // Default checks

                action {
                    queryFields(MojangNamespace, arguments.query, arguments.version, arguments.channel?.str)
                }
            }

            // Method
            chatCommand(::MojangArguments) {
                name = "mmm"
                aliases = arrayOf("mojm", "mojmapm")

                description = "Look up Mojang mappings info for a method.\n\n" +

                    "**Channels:** " + Channels.values().joinToString(", ") { "`${it.str}`" } +
                    "\n\n" +

                    "For more information or a list of versions for Mojang mappings, you can use the `mojang` " +
                    "command."

                check { customChecks(name, MojangNamespace) }
                check(categoryCheck, channelCheck, guildCheck)  // Default checks

                action {
                    queryMethods(MojangNamespace, arguments.query, arguments.version, arguments.channel?.str)
                }
            }
        }

        // endregion

        // region: Hashed Mojang mappings lookups

        if (hashedMojangEnabled) {
            // Class
            chatCommand(::HashedMojangArguments) {
                name = "hc"
                aliases = arrayOf("hmojc", "hmojmapc", "hmc", "qhc")

                description = "Look up Hashed Mojang mappings info for a class.\n\n" +

                    "**Channels:** " + Channels.values().joinToString(", ") { "`${it.str}`" } +
                    "\n\n" +

                    "For more information or a list of versions for Mojang mappings, you can use the `mojang` " +
                    "command."

                check { customChecks(name, MojangHashedNamespace) }
                check(categoryCheck, channelCheck, guildCheck)  // Default checks

                action {
                    queryClasses(MojangHashedNamespace, arguments.query, arguments.version, arguments.channel?.str)
                }
            }

            // Field
            chatCommand(::HashedMojangArguments) {
                name = "hf"
                aliases = arrayOf("hmojf", "hmojmapf", "hmf", "qhf")

                description = "Look up Hashed Mojang mappings info for a field.\n\n" +

                    "**Channels:** " + Channels.values().joinToString(", ") { "`${it.str}`" } +
                    "\n\n" +

                    "For more information or a list of versions for Mojang mappings, you can use the `mojang` " +
                    "command."

                check { customChecks(name, MojangHashedNamespace) }
                check(categoryCheck, channelCheck, guildCheck)  // Default checks

                action {
                    queryFields(MojangHashedNamespace, arguments.query, arguments.version, arguments.channel?.str)
                }
            }

            // Method
            chatCommand(::HashedMojangArguments) {
                name = "hm"
                aliases = arrayOf("hmojm", "hmojmapm", "hmm", "qhm")

                description = "Look up Hashed Mojang mappings info for a method.\n\n" +

                    "**Channels:** " + Channels.values().joinToString(", ") { "`${it.str}`" } +
                    "\n\n" +

                    "For more information or a list of versions for Mojang mappings, you can use the `mojang` " +
                    "command."

                check { customChecks(name, MojangHashedNamespace) }
                check(categoryCheck, channelCheck, guildCheck)  // Default checks

                action {
                    queryMethods(MojangHashedNamespace, arguments.query, arguments.version, arguments.channel?.str)
                }
            }
        }

        // endregion

        // region: Plasma mappings lookups

        if (plasmaEnabled) {
            // Class
            chatCommand(::PlasmaArguments) {
                name = "pc"

                description = "Look up Plasma mappings info for a class.\n\n" +

                    "For more information or a list of versions for Plasma mappings, you can use the " +
                    "`plasma` command."

                check { customChecks(name, PlasmaNamespace) }
                check(categoryCheck, channelCheck, guildCheck)  // Default checks

                action {
                    queryClasses(PlasmaNamespace, arguments.query, arguments.version)
                }
            }

            // Field
            chatCommand(::PlasmaArguments) {
                name = "pf"

                description = "Look up Plasma mappings info for a field.\n\n" +

                    "For more information or a list of versions for Plasma mappings, you can use the " +
                    "`plasma` command."

                check { customChecks(name, PlasmaNamespace) }
                check(categoryCheck, channelCheck, guildCheck)  // Default checks

                action {
                    queryFields(PlasmaNamespace, arguments.query, arguments.version)
                }
            }

            // Method
            chatCommand(::PlasmaArguments) {
                name = "pm"

                description = "Look up Plasma mappings info for a method.\n\n" +

                    "For more information or a list of versions for Plasma mappings, you can use the " +
                    "`plasma` command."

                check { customChecks(name, PlasmaNamespace) }
                check(categoryCheck, channelCheck, guildCheck)  // Default checks

                action {
                    queryMethods(PlasmaNamespace, arguments.query, arguments.version)
                }
            }
        }

        // endregion

        // region: Yarn mappings lookups

        if (yarnEnabled) {
            // Class
            chatCommand({ YarnArguments(patchworkEnabled) }) {
                name = "yc"
                aliases = arrayOf("yarnc")

                description = "Look up Yarn mappings info for a class.\n\n" +

                    "**Channels:** $yarnChannels" +
                    "\n\n" +

                    "For more information or a list of versions for Yarn mappings, you can use the `yarn` " +
                    "command."

                check { customChecks(name, YarnNamespace) }
                check(categoryCheck, channelCheck, guildCheck)  // Default checks

                action {
                    if (!patchworkEnabled && arguments.channel == YarnChannels.PATCHWORK) {
                        message.respond("Patchwork support is currently disabled.")
                        return@action
                    }

                    queryClasses(YarnNamespace, arguments.query, arguments.version, arguments.channel?.str)
                }
            }

            // Field
            chatCommand({ YarnArguments(patchworkEnabled) }) {
                name = "yf"
                aliases = arrayOf("yarnf")

                description = "Look up Yarn mappings info for a field.\n\n" +

                    "**Channels:** $yarnChannels" +
                    "\n\n" +

                    "For more information or a list of versions for Yarn mappings, you can use the `yarn` " +
                    "command."

                check { customChecks(name, YarnNamespace) }
                check(categoryCheck, channelCheck, guildCheck)  // Default checks

                action {
                    if (!patchworkEnabled && arguments.channel == YarnChannels.PATCHWORK) {
                        message.respond("Patchwork support is currently disabled.")
                        return@action
                    }

                    queryFields(YarnNamespace, arguments.query, arguments.version, arguments.channel?.str)
                }
            }

            // Method
            chatCommand({ YarnArguments(patchworkEnabled) }) {
                name = "ym"
                aliases = arrayOf("yarnm")

                description = "Look up Yarn mappings info for a method.\n\n" +

                    "**Channels:** $yarnChannels" +
                    "\n\n" +

                    "For more information or a list of versions for Yarn mappings, you can use the `yarn` " +
                    "command."

                check { customChecks(name, YarnNamespace) }
                check(categoryCheck, channelCheck, guildCheck)  // Default checks

                action {
                    if (!patchworkEnabled && arguments.channel == YarnChannels.PATCHWORK) {
                        message.respond("Patchwork support is currently disabled.")
                        return@action
                    }

                    queryMethods(YarnNamespace, arguments.query, arguments.version, arguments.channel?.str)
                }
            }
        }

        // endregion

        // region: Yarrn mappings lookups

        if (yarrnEnabled) {
            // Class
            chatCommand(::YarrnArguments) {
                name = "yrc"

                description = "Look up Yarrn mappings info for a class.\n\n" +

                    "For more information or a list of versions for Yarrn mappings, you can use the " +
                    "`yarrn` command."

                check { customChecks(name, YarrnNamespace) }
                check(categoryCheck, channelCheck, guildCheck)  // Default checks

                action {
                    queryClasses(YarrnNamespace, arguments.query, arguments.version)
                }
            }

            // Field
            chatCommand(::YarrnArguments) {
                name = "yrf"

                description = "Look up Yarrn mappings info for a field.\n\n" +

                    "For more information or a list of versions for Yarrn mappings, you can use the " +
                    "`yarrn` command."

                check { customChecks(name, YarrnNamespace) }
                check(categoryCheck, channelCheck, guildCheck)  // Default checks

                action {
                    queryFields(YarrnNamespace, arguments.query, arguments.version)
                }
            }

            // Method
            chatCommand(::YarrnArguments) {
                name = "yrm"

                description = "Look up Yarrn mappings info for a method.\n\n" +

                    "For more information or a list of versions for Yarrn mappings, you can use the " +
                    "`yarrn` command."

                check { customChecks(name, YarrnNamespace) }
                check(categoryCheck, channelCheck, guildCheck)  // Default checks

                action {
                    queryMethods(YarrnNamespace, arguments.query, arguments.version)
                }
            }
        }

        // endregion

        // region: Mappings info commands

        if (legacyYarnEnabled) {
            chatCommand {
                name = "lyarn"
                aliases = arrayOf("legacy-yarn", "legacyyarn", "legacyarn")

                description = "Get information and a list of supported versions for Legacy Yarn mappings."

                check { customChecks(name, LegacyYarnNamespace) }
                check(categoryCheck, channelCheck, guildCheck)  // Default checks

                action {
                    val defaultVersion = LegacyYarnNamespace.getDefaultVersion()
                    val allVersions = LegacyYarnNamespace.getAllSortedVersions()

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
                        "Legacy Yarn mappings are available for queries across **${allVersions.size}** " +
                            "versions.\n\n" +

                            "**Default version:** $defaultVersion\n" +
                            "**Commands:** `lyc`, `lyf`, `lym`\n\n" +

                            "For a full list of supported Yarn versions, please view the rest of the pages."
                    )

                    val pagesObj = Pages()
                    val pageTitle = "Mappings info: Legacy Yarn"

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

                    val paginator = MessageButtonPaginator(
                        targetMessage = event.message,
                        pages = pagesObj,
                        keepEmbed = true,
                        owner = message.author,
                        timeoutSeconds = getTimeout(),
                        locale = getLocale(),
                    )

                    paginator.send()
                }
            }
        }

        if (mcpEnabled) {
            chatCommand {
                name = "mcp"

                description = "Get information and a list of supported versions for MCP mappings."

                check { customChecks(name, MCPNamespace) }
                check(categoryCheck, channelCheck, guildCheck)  // Default checks

                action {
                    val defaultVersion = MCPNamespace.getDefaultVersion()
                    val allVersions = MCPNamespace.getAllSortedVersions()

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
                        "MCP mappings are available for queries across **${allVersions.size}** versions.\n\n" +

                            "**Default version:** $defaultVersion\n" +
                            "**Commands:** `mcpc`, `mcpf`, `mcpm`\n\n" +

                            "For a full list of supported MCP versions, please view the rest of the pages."
                    )

                    val pagesObj = Pages()
                    val pageTitle = "Mappings info: MCP"

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

                    val paginator = MessageButtonPaginator(
                        targetMessage = event.message,
                        pages = pagesObj,
                        keepEmbed = true,
                        owner = message.author,
                        timeoutSeconds = getTimeout(),
                        locale = getLocale(),
                    )

                    paginator.send()
                }
            }
        }

        if (mojangEnabled) {
            chatCommand {
                name = "mojang"
                aliases = arrayOf("mojmap")

                description = "Get information and a list of supported versions for Mojang mappings."

                check { customChecks(name, MojangNamespace) }
                check(categoryCheck, channelCheck, guildCheck)  // Default checks

                action {
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

                            "**Channels:** " + Channels.values().joinToString(", ") { "`${it.str}`" } +
                            "\n" +
                            "**Commands:** `mmc`, `mmf`, `mmm`\n\n" +

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

                    val paginator = MessageButtonPaginator(
                        targetMessage = event.message,
                        pages = pagesObj,
                        keepEmbed = true,
                        owner = message.author,
                        timeoutSeconds = getTimeout(),
                        locale = getLocale(),
                    )

                    paginator.send()
                }
            }
        }

        if (hashedMojangEnabled) {
            chatCommand {
                name = "hashed"
                aliases = arrayOf("hashed-mojmap", "hashed-mojang", "quilt-hashed", "qh", "hm")

                description = "Get information and a list of supported versions for hashed Mojang mappings."

                check { customChecks(name, MojangHashedNamespace) }
                check(categoryCheck, channelCheck, guildCheck)  // Default checks

                action {
                    val defaultVersion = MojangHashedNamespace.getDefaultVersion()
                    val allVersions = MojangHashedNamespace.getAllSortedVersions()

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
                        "Hashed Mojang mappings are available for queries across **${allVersions.size}** " +
                            "versions.\n\n" +

                            "**Default version:** $defaultVersion\n\n" +

                            "**Channels:** " + Channels.values().joinToString(", ") { "`${it.str}`" } +
                            "\n" +
                            "**Commands:** `hc`, `hf`, `hm`\n\n" +

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

                    val paginator = MessageButtonPaginator(
                        targetMessage = event.message,
                        pages = pagesObj,
                        keepEmbed = true,
                        owner = message.author,
                        timeoutSeconds = getTimeout(),
                        locale = getLocale(),
                    )

                    paginator.send()
                }
            }
        }

        if (plasmaEnabled) {
            chatCommand {
                name = "plasma"

                description = "Get information and a list of supported versions for Plasma mappings."

                check { customChecks(name, PlasmaNamespace) }
                check(categoryCheck, channelCheck, guildCheck)  // Default checks

                action {
                    val defaultVersion = PlasmaNamespace.getDefaultVersion()
                    val allVersions = PlasmaNamespace.getAllSortedVersions()

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
                        "Plasma mappings are available for queries across **${allVersions.size}** " +
                            "versions.\n\n" +

                            "**Default version:** $defaultVersion\n" +
                            "**Commands:** `pc`, `pf`, `pm`\n\n" +

                            "For a full list of supported Plasma versions, please view the rest of the pages."
                    )

                    val pagesObj = Pages()
                    val pageTitle = "Mappings info: Plasma"

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

                    val paginator = MessageButtonPaginator(
                        targetMessage = event.message,
                        pages = pagesObj,
                        keepEmbed = true,
                        owner = message.author,
                        timeoutSeconds = getTimeout(),
                        locale = getLocale(),
                    )

                    paginator.send()
                }
            }
        }

        if (yarnEnabled) {
            chatCommand {
                name = "yarn"

                description = "Get information and a list of supported versions for Yarn mappings."

                check { customChecks(name, YarnNamespace) }
                check(categoryCheck, channelCheck, guildCheck)  // Default checks

                action {
                    val defaultPatchworkVersion = if (patchworkEnabled) {
                        YarnNamespace.getDefaultVersion { YarnChannels.PATCHWORK.str }
                    } else {
                        ""
                    }

                    val defaultVersion = YarnNamespace.getDefaultVersion()
                    val defaultSnapshotVersion = YarnNamespace.getDefaultVersion { YarnChannels.SNAPSHOT.str }
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
                            "**Commands:** `yc`, `yf`, `ym`\n\n" +

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

                    val paginator = MessageButtonPaginator(
                        targetMessage = event.message,
                        pages = pagesObj,
                        keepEmbed = true,
                        owner = message.author,
                        timeoutSeconds = getTimeout(),
                        locale = getLocale(),
                    )

                    paginator.send()
                }
            }
        }

        if (yarrnEnabled) {
            chatCommand {
                name = "yarrn"

                description = "Get information and a list of supported versions for Yarrn mappings."

                check { customChecks(name, YarrnNamespace) }
                check(categoryCheck, channelCheck, guildCheck)  // Default checks

                action {
                    val defaultVersion = YarrnNamespace.getDefaultVersion()
                    val allVersions = YarrnNamespace.getAllSortedVersions()

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
                        "Yarrn mappings are available for queries across **${allVersions.size}** " +
                            "versions.\n\n" +

                            "**Default version:** $defaultVersion\n" +
                            "**Commands:** `yrc`, `yrf`, `yrm`\n\n" +

                            "For a full list of supported Yarrn versions, please view the rest of the pages."
                    )

                    val pagesObj = Pages()
                    val pageTitle = "Mappings info: Yarrn"

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

                    val paginator = MessageButtonPaginator(
                        targetMessage = event.message,
                        pages = pagesObj,
                        keepEmbed = true,
                        owner = message.author,
                        timeoutSeconds = getTimeout(),
                        locale = getLocale(),
                    )

                    paginator.send()
                }
            }
        }

        // endregion

        logger.info { "Mappings extension set up - namespaces: " + enabledNamespaces.joinToString(", ") }
    }

    private suspend fun ChatCommandContext<out Arguments>.queryClasses(
        namespace: Namespace,
        givenQuery: String,
        version: MappingsContainer?,
        channel: String? = null
    ) {
        sentry.breadcrumb(BreadcrumbType.Query) {
            message = "Beginning class lookup"

            data["channel"] = channel ?: "N/A"
            data["namespace"] = namespace.id
            data["query"] = givenQuery
            data["version"] = version?.version ?: "N/A"
        }

        val context = newSingleThreadContext("c: $givenQuery")

        try {
            withContext(context) {
                val provider = if (version == null) {
                    if (channel != null) {
                        namespace.getProvider(
                            namespace.getDefaultVersion { channel }
                        )
                    } else {
                        MappingsProvider.empty(namespace)
                    }
                } else {
                    namespace.getProvider(version.version)
                }

                provider.injectDefaultVersion(
                    namespace.getDefaultProvider {
                        channel ?: namespace.getDefaultMappingChannel()
                    }
                )

                sentry.breadcrumb(BreadcrumbType.Info) {
                    message = "Provider resolved, with injected default version"

                    data["version"] = provider.version ?: "Unknown"
                }

                val query = givenQuery.replace(".", "/")
                var pages: List<Pair<String, String>>

                sentry.breadcrumb(BreadcrumbType.Info) {
                    message = "Attempting to run sanitized query"

                    data["query"] = query
                }

                message.channel.withTyping {
                    @Suppress("TooGenericExceptionCaught")
                    val result = try {
                        MappingsQuery.queryClasses(
                            QueryContext(
                                provider = provider,
                                searchKey = query
                            )
                        )
                    } catch (e: NullPointerException) {
                        message.respond(e.localizedMessage)
                        return@withContext
                    }

                    sentry.breadcrumb(BreadcrumbType.Info) {
                        message = "Generating pages for results"

                        data["resultCount"] = result.value.size
                    }

                    pages = classesToPages(namespace, result)
                }

                if (pages.isEmpty()) {
                    message.respond("No results found")
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

                val paginator = MessageButtonPaginator(
                    targetMessage = event.message,
                    pages = pagesObj,
                    keepEmbed = true,
                    owner = message.author,
                    timeoutSeconds = getTimeout(),
                    locale = getLocale(),
                )

                paginator.send()
            }
        } finally {
            context.close()
        }
    }

    private suspend fun ChatCommandContext<out Arguments>.queryFields(
        namespace: Namespace,
        givenQuery: String,
        version: MappingsContainer?,
        channel: String? = null
    ) {
        sentry.breadcrumb(BreadcrumbType.Query) {
            message = "Beginning field lookup"

            data["channel"] = channel ?: "N/A"
            data["namespace"] = namespace.id
            data["query"] = givenQuery
            data["version"] = version?.version ?: "N/A"
        }

        val context = newSingleThreadContext("f: $givenQuery")

        try {
            withContext(context) {
                val provider = if (version == null) {
                    if (channel != null) {
                        namespace.getProvider(
                            namespace.getDefaultVersion { channel }
                        )
                    } else {
                        MappingsProvider.empty(namespace)
                    }
                } else {
                    namespace.getProvider(version.version)
                }

                provider.injectDefaultVersion(
                    namespace.getDefaultProvider {
                        channel ?: namespace.getDefaultMappingChannel()
                    }
                )

                sentry.breadcrumb(BreadcrumbType.Info) {
                    message = "Provider resolved, with injected default version"

                    data["version"] = provider.version ?: "Unknown"
                }

                val query = givenQuery.replace(".", "/")
                var pages: List<Pair<String, String>>

                sentry.breadcrumb(BreadcrumbType.Info) {
                    message = "Attempting to run sanitized query"

                    data["query"] = query
                }

                message.channel.withTyping {
                    @Suppress("TooGenericExceptionCaught")
                    val result = try {
                        MappingsQuery.queryFields(
                            QueryContext(
                                provider = provider,
                                searchKey = query
                            )
                        )
                    } catch (e: NullPointerException) {
                        message.respond(e.localizedMessage)
                        return@withContext
                    }

                    sentry.breadcrumb(BreadcrumbType.Info) {
                        message = "Generating pages for results"

                        data["resultCount"] = result.value.size
                    }

                    pages = fieldsToPages(namespace, provider.get(), result)
                }

                if (pages.isEmpty()) {
                    message.respond("No results found")
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

                val paginator = MessageButtonPaginator(
                    targetMessage = event.message,
                    pages = pagesObj,
                    keepEmbed = true,
                    owner = message.author,
                    timeoutSeconds = getTimeout(),
                    locale = getLocale(),
                )

                paginator.send()
            }
        } finally {
            context.close()
        }
    }

    private suspend fun ChatCommandContext<out Arguments>.queryMethods(
        namespace: Namespace,
        givenQuery: String,
        version: MappingsContainer?,
        channel: String? = null
    ) {
        sentry.breadcrumb(BreadcrumbType.Query) {
            message = "Beginning method lookup"

            data["channel"] = channel ?: "N/A"
            data["namespace"] = namespace.id
            data["query"] = givenQuery
            data["version"] = version?.version ?: "N/A"
        }

        val context = newSingleThreadContext("m: $givenQuery")

        try {
            withContext(context) {
                val provider = if (version == null) {
                    if (channel != null) {
                        namespace.getProvider(
                            namespace.getDefaultVersion { channel }
                        )
                    } else {
                        MappingsProvider.empty(namespace)
                    }
                } else {
                    namespace.getProvider(version.version)
                }

                provider.injectDefaultVersion(
                    namespace.getDefaultProvider {
                        channel ?: namespace.getDefaultMappingChannel()
                    }
                )

                sentry.breadcrumb(BreadcrumbType.Info) {
                    message = "Provider resolved, with injected default version"

                    data["version"] = provider.version ?: "Unknown"
                }

                val query = givenQuery.replace(".", "/")
                var pages: List<Pair<String, String>>

                sentry.breadcrumb(BreadcrumbType.Info) {
                    message = "Attempting to run sanitized query"

                    data["query"] = query
                }

                message.channel.withTyping {
                    @Suppress("TooGenericExceptionCaught")
                    val result = try {
                        MappingsQuery.queryMethods(
                            QueryContext(
                                provider = provider,
                                searchKey = query
                            )
                        )
                    } catch (e: NullPointerException) {
                        message.respond(e.localizedMessage)
                        return@withContext
                    }

                    sentry.breadcrumb(BreadcrumbType.Info) {
                        message = "Generating pages for results"

                        data["resultCount"] = result.value.size
                    }

                    pages = methodsToPages(namespace, provider.get(), result)
                }

                if (pages.isEmpty()) {
                    message.respond("No results found")
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

                val paginator = MessageButtonPaginator(
                    targetMessage = event.message,
                    pages = pagesObj,
                    keepEmbed = true,
                    owner = message.author,
                    timeoutSeconds = getTimeout(),
                    locale = getLocale(),
                )

                paginator.send()
            }
        } finally {
            context.close()
        }
    }

    private suspend fun getTimeout() = builder.config.getTimeout()

    private suspend fun CheckContext<MessageCreateEvent>.customChecks(command: String, namespace: Namespace) {
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
