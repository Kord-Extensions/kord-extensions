/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.kordex.modules.func.tags

import dev.kordex.core.builders.ExtensibleBotBuilder
import dev.kordex.core.utils.loadModule
import dev.kordex.modules.func.tags.config.SimpleTagsConfig
import dev.kordex.modules.func.tags.config.TagsConfig
import dev.kordex.modules.func.tags.data.TagsData
import org.koin.dsl.bind

fun ExtensibleBotBuilder.ExtensionsBuilder.tags(config: TagsConfig, data: TagsData) {
	loadModule { single { config } bind TagsConfig::class }
	loadModule { single { data } bind TagsData::class }

	add { TagsExtension() }
}

fun ExtensibleBotBuilder.ExtensionsBuilder.tags(data: TagsData, body: SimpleTagsConfig.Builder.() -> Unit) {
	tags(SimpleTagsConfig(body), data)
}

fun String?.nullIfBlank(): String? =
	if (isNullOrBlank()) {
		null
	} else {
		this
	}
