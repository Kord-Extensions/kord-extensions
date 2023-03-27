/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.modules.extra.mappings.utils

import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import me.shedaniel.linkie.MappingsBuilder
import me.shedaniel.linkie.MappingsSource
import me.shedaniel.linkie.Namespace
import me.shedaniel.linkie.namespaces.MCPNamespace
import me.shedaniel.linkie.namespaces.MCPNamespace.MCPVersion
import me.shedaniel.linkie.parser.apply
import me.shedaniel.linkie.parser.srg
import me.shedaniel.linkie.parser.tsrg
import me.shedaniel.linkie.utils.*
import java.net.URL

/**
 * A replacement for Linkie's default [MCPNamespace],
 * which fixes issues due to [mcp export servers](http://export.mcpbot.bspk.rs)
 * being down indefinitely.
 */
object McpNamespaceReplacement : Namespace("mcp") {
    private const val forgeMaven = "http://maven.minecraftforge.net/de/oceanlabs/mcp"
    private const val mcpArchive = "https://raw.githubusercontent.com/ModCoderPack/MCPMappingsArchive/master"
    private val mcpConfigSnapshots = mutableMapOf<Version, MutableList<String>>()
    private val newMcpVersions = mutableMapOf<Version, MCPVersion>()

    init {
        buildSupplier {
            cached()

            buildVersions {
                versionsSeq(::getAllBotVersions)
                uuid { "$it-${mcpConfigSnapshots[it.toVersion()]?.maxOrNull()!!}" }

                buildMappings(name = "MCP") {
                    val latestSnapshot = mcpConfigSnapshots[it.toVersion()]?.maxOrNull()!!
                    @Suppress("MagicNumber") // we're constructing a version, we need 13
                    source(
                        if (it.toVersion() >= Version(1, 13)) {
                            loadTsrgFromURLZip(URL("$forgeMaven/mcp_config/$it/mcp_config-$it.zip"))
                            MappingsSource.MCP_TSRG
                        } else {
                            val link = "$forgeMaven/mcp/$it/mcp-$it-srg.zip"
                            loadSrgFromURLZip(URL(link))
                            MappingsSource.MCP_SRG
                        }
                    )
                    val link = "$mcpArchive/mcp_snapshot/$latestSnapshot-$it/mcp_snapshot-$latestSnapshot-$it.zip"
                    loadMCPFromURLZip(URL(link))
                }
            }

            buildVersions {
                versions { newMcpVersions.keys.map(Version::toString) }
                uuid { newMcpVersions[it.toVersion()]!!.name }

                buildMappings(name = "MCP") {
                    val mcpVersion = newMcpVersions[it.toVersion()]!!
                    loadTsrgFromURLZip(URL(mcpVersion.mcp_config))
                    loadMCPFromURLZip(URL(mcpVersion.mcp))
                    source(MappingsSource.MCP_TSRG)
                }
            }
        }
    }

    /** @suppress **/
    fun getAllBotVersions(): Sequence<String> = mcpConfigSnapshots.keys.asSequence().map { it.toString() }

    override fun supportsFieldDescription(): Boolean = false
    override fun getDefaultLoadedVersions(): List<String> = listOf(defaultVersion)
    override fun getAllVersions(): Sequence<String> = getAllBotVersions() + newMcpVersions.keys.map(Version::toString)

    override fun supportsAT(): Boolean = true
    override fun supportsMixin(): Boolean = true
    override suspend fun reloadData() {
        mcpConfigSnapshots.clear()

        val versionsJson = URL("$mcpArchive/versions.json").readText()

        json.parseToJsonElement(versionsJson).jsonObject.forEach { mcVersion, mcpVersionsObj ->
            val list = mcpConfigSnapshots.getOrPut(mcVersion.toVersion()) { mutableListOf() }
            mcpVersionsObj.jsonObject["snapshot"]?.jsonArray?.forEach {
                list.add(it.jsonPrimitive.content)
            }
        }
        mcpConfigSnapshots.filterValues { it.isEmpty() }.keys.toMutableList().forEach {
            mcpConfigSnapshots.remove(it)
        }

        newMcpVersions.clear()
        val tmpMcpVersionsJson = json.decodeFromString(
            MapSerializer(String.serializer(), MCPVersion.serializer()),
            URL(MCPNamespace.tmpMcpVersionsUrl).readText()
        )

        tmpMcpVersionsJson.forEach { (mcVersion, mcpVersion) ->
            newMcpVersions[mcVersion.toVersion()] = mcpVersion
        }
    }

    /** @suppress **/
    suspend fun MappingsBuilder.loadTsrgFromURLZip(url: URL) {
        url.toAsyncZip().forEachEntry { path, entry ->
            if (!entry.isDirectory && path.split("/").lastOrNull() == "joined.tsrg") {
                loadTsrgFromInputStream(entry.bytes.decodeToString())
            }
        }
    }

    /** @suppress **/
    suspend fun MappingsBuilder.loadSrgFromURLZip(url: URL) {
        url.toAsyncZip().forEachEntry { path, entry ->
            if (!entry.isDirectory && path.split("/").lastOrNull() == "joined.srg") {
                loadSrgFromInputStream(entry.bytes.decodeToString())
            }
        }
    }

    private fun MappingsBuilder.loadSrgFromInputStream(content: String) {
        apply(
            srg(content),
            obfMerged = "obf",
            intermediary = "srg",
        )
    }

    private fun MappingsBuilder.loadTsrgFromInputStream(content: String) {
        apply(
            tsrg(content),
            obfMerged = "obf",
            intermediary = "srg",
        )
    }

    /** @suppress **/
    suspend fun MappingsBuilder.loadMCPFromURLZip(url: URL) {
        url.toAsyncZip().forEachEntry { path, entry ->
            if (!entry.isDirectory) {
                when (path.split("/").lastOrNull()) {
                    "fields.csv" -> loadMCPFieldsCSVFromInputStream(entry.bytes.lines())
                    "methods.csv" -> loadMCPMethodsCSVFromInputStream(entry.bytes.lines())
                }
            }
        }
    }

    private fun MappingsBuilder.loadMCPFieldsCSVFromInputStream(lines: Sequence<String>) {
        val map = mutableMapOf<String, String>()
        lines.filterNotBlank().forEach {
            val split = it.split(',')
            map[split[0]] = split[1]
        }
        container.classes.forEach { (_, it) ->
            it.fields.forEach { field ->
                map[field.intermediaryName]?.apply {
                    field.mappedName = this
                }
            }
        }
    }

    private fun MappingsBuilder.loadMCPMethodsCSVFromInputStream(lines: Sequence<String>) {
        val map = mutableMapOf<String, String>()
        lines.filterNotBlank().forEach {
            val split = it.split(',')
            map[split[0]] = split[1]
        }
        container.classes.forEach { (_, it) ->
            it.methods.forEach { method ->
                map[method.intermediaryName]?.apply {
                    method.mappedName = this
                }
            }
        }
    }
}
