/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.modules.func.tags

import dev.kordex.core.builders.ExtensionsBuilder
import dev.kordex.core.utils.loadModule
import dev.kordex.modules.func.tags.config.SimpleTagsConfig
import dev.kordex.modules.func.tags.config.TagsConfig
import dev.kordex.modules.func.tags.data.TagsData
import org.koin.dsl.bind

fun ExtensionsBuilder.tags(config: TagsConfig, data: TagsData) {
	loadModule { single { config } bind TagsConfig::class }
	loadModule { single { data } bind TagsData::class }

	add { TagsExtension() }
}

fun ExtensionsBuilder.tags(data: TagsData, body: SimpleTagsConfig.Builder.() -> Unit) {
	tags(SimpleTagsConfig(body), data)
}

fun String?.nullIfBlank(): String? =
	if (isNullOrBlank()) {
		null
	} else {
		this
	}
