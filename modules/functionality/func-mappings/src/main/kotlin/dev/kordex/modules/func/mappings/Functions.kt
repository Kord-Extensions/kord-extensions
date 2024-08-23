/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.modules.func.mappings

import dev.kordex.core.builders.AboutBuilder
import dev.kordex.core.builders.ExtensionsBuilder
import dev.kordex.core.builders.about.CopyrightType
import dev.kordex.modules.func.mappings.builders.ExtMappingsBuilder

private var copyrightAdded = false

/**
 * Configure the mappings extension and add it to the bot.
 */
fun ExtensionsBuilder.extMappings(builder: ExtMappingsBuilder.() -> Unit) {
	val obj = ExtMappingsBuilder()

	builder(obj)
	MappingsExtension.configure(obj)

	add(::MappingsExtension)
}

internal fun AboutBuilder.addCopyright() {
	if (!copyrightAdded) {
		copyright(
			"Linkie: Core",
			"Apache-2.0",
			CopyrightType.Library,
			"https://linkie.shedaniel.dev/"
		)
	}

	copyrightAdded = true
}
