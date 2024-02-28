package com.kotlindiscord.kord.extensions.plugins

import java.net.URL
import java.net.URLClassLoader

public class PluginClassLoader(
	public val jarPath: String,
	private val parent: ParentPluginClassLoader,
) : ClassLoader() {
	private val urlClassLoader = URLClassLoader(
		arrayOf(URL("file://${parent.pluginsDirectory}/$jarPath")),
		null
	)

	internal fun loadClassWithoutRecursion(name: String?): Class<*> =
		urlClassLoader.loadClass(name)

	override fun loadClass(name: String?): Class<*> {
		return try {
			urlClassLoader.loadClass(name)
		} catch (e: ClassNotFoundException) {
			parent.loadClass(name)
		}
	}

	public fun close() {
		urlClassLoader.close()
	}
}
