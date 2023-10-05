@file:Suppress("TooGenericExceptionCaught")

package com.kotlindiscord.kord.extensions.plugins

import com.kotlindiscord.kord.extensions.plugins.constraints.ConstraintChecker
import com.kotlindiscord.kord.extensions.plugins.constraints.ConstraintResult
import com.kotlindiscord.kord.extensions.plugins.types.PluginManifest
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.z4kn4fein.semver.Version
import kotlinx.serialization.json.Json
import java.io.File
import java.lang.ref.WeakReference
import java.nio.file.FileSystems
import kotlin.io.path.exists
import kotlin.io.path.readText

public class PluginManager<T : Plugin<T>>(
	public val baseTypeReference: String,
	public val extraVersions: Map<String, Version> = mapOf(),
	public val errorOnFailedConstraint: Boolean = true,

	pluginDirectory: String = "plugins",
	constraintCheckerCallback: (PluginManager<*>) -> ConstraintChecker = ::ConstraintChecker,
) {
	private val logger = KotlinLogging.logger {}
	private val constraintChecker = constraintCheckerCallback(this)

	public val pluginsDirectory: String = pluginDirectory.trimEnd('/', '\\')

	private val classLoader: ParentPluginClassLoader = ParentPluginClassLoader(pluginsDirectory)
	private val pluginManifests: MutableMap<String, PluginManifest> = mutableMapOf()

	private val plugins: MutableMap<String, T> = mutableMapOf()

	@Suppress("UNCHECKED_CAST")
	public fun loadPlugin(pluginId: String, setup: Boolean = true, checkManifest: Boolean = true): WeakReference<T>? {
		if (pluginId in plugins) {
			return WeakReference(plugins[pluginId]!!)
		}

		val manifest = pluginManifests[pluginId]
			?: return null

		if (checkManifest) {
			val results = constraintChecker.checkPlugin(pluginId, pluginManifests)

			if (results.isNotEmpty()) {
				logConstraintResults(results)

				return null
			}
		}

		if (manifest.failed) {
			logger.warn { "Not loading \"$pluginId\" as it failed to validate" }

			return null
		}

		classLoader.loadPluginLoader(pluginId, manifest.jarPath)

		val loadedClass = try {
			classLoader.loadClass(manifest.classRef).getDeclaredConstructor().newInstance() as T?
		} catch (e: ClassNotFoundException) {
			logger.error { "Failed to load \"$pluginId\": Class not found" }

			return null
		}

		if (loadedClass == null) {
			logger.error { "Class ${manifest.classRef} for \"$pluginId\" does not extend $baseTypeReference" }

			return null
		}

		loadedClass.manifest = manifest
		loadedClass.pluginManager = this

		plugins[pluginId] = loadedClass

		if (setup) {
			try {
				loadedClass.internalLoad()
			} catch (e: Exception) {
				logger.error(e) { "Failed to set up plugin \"$pluginId\", load function threw an exception" }
				unloadPlugin(pluginId)

				return null
			}
		}

		logger.debug { "Loaded \"$pluginId\"" }

		return WeakReference(loadedClass)
	}

	public fun unloadPlugin(pluginId: String): Boolean {
		if (pluginId in plugins) {
			try {
				plugins[pluginId]!!.internalUnload()
			} catch (e: Exception) {
				logger.error(e) { "Failed to properly unload plugin \"$pluginId\", unload function threw an exception" }
			}

			plugins.remove(pluginId)
			classLoader.removePluginLoader(pluginId)

			return true
		}

		return false
	}

	public fun loadAllPlugins() {
		reloadManifests()

		val pluginsToSetup = pluginManifests.keys.filter { it !in plugins }

		for (manifest in pluginManifests.values) {
			try {
				loadPlugin(manifest.id, setup = false, checkManifest = false)
			} catch (e: Exception) {
				logger.error(e) { "Failed to load plugin ${manifest.id} from ${manifest.jarPath}" }
			}
		}

		pluginsToSetup.forEach {
			try {
				plugins[it]?.internalLoad()
			} catch (e: Exception) {
				logger.error(e) { "Failed to set up plugin \"$it\", load function threw an exception" }

				unloadPlugin(it)
			}
		}
	}

	public fun reloadManifests(): Boolean {
		val files = File(pluginsDirectory).listFiles()

		if (files == null) {
			logger.error { "Plugin directory $pluginsDirectory does not exist or is inaccessible" }

			return false
		}

		val foundManifests: MutableList<PluginManifest> = mutableListOf()

		for (jarFile in files.filter { file -> file.extension == "jar" }) {
			val zipFileSystem = FileSystems.newFileSystem(jarFile.toPath(), emptyMap<String, String>())
			val path = zipFileSystem.getPath("kordex.plugin.json")

			if (!path.exists()) {
				logger.warn { "Skipping ${jarFile.name} | JAR file does not contain a kordex.plugin.json file" }

				continue
			}

			val manifest: PluginManifest = try {
				Json.decodeFromString(path.readText())
			} catch (e: Exception) {
				logger.error(e) { "Skipping ${jarFile.name} | Failed to load invalid manifest in kordex.plugin.json" }

				continue
			}

			zipFileSystem.close()

			manifest.jarPath = jarFile.name

			pluginManifests[manifest.id] = manifest
			foundManifests.add(manifest)
		}

		val results = constraintChecker.checkAll(pluginManifests)

		if (results.isNotEmpty()) {
			logConstraintResults(results)

			if (errorOnFailedConstraint) {
				error("Failed to load plugins - check the log for more information.")
			}
		}

		logger.info { "Loaded ${foundManifests.size} plugin manifests" }

		return true
	}

	public fun logConstraintResults(results: MutableMap<String, List<ConstraintResult>>) {
		val length = results.keys.maxBy { it.length }.length

		logger.error {
			buildString {
				appendLine("Plugin constraint errors detected:")
				appendLine()

				results.forEach { (pluginId, resultList) ->
					resultList.forEach { result ->
						appendLine("${pluginId.padEnd(length)} | ${result.readableString}")
					}
				}
			}
		}
	}

	public fun logConstraintResults(results: List<ConstraintResult>) {
		logger.error {
			buildString {
				appendLine("Plugin constraint errors detected:")
				appendLine()

				results.forEach { result ->
					appendLine("${result.pluginId} | ${result.readableString}")
				}
			}
		}
	}
}
