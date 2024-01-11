/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.modules.extra.tags

import com.kotlindiscord.kord.extensions.builders.ExtensibleBotBuilder
import com.kotlindiscord.kord.extensions.modules.extra.tags.config.SimpleTagsConfig
import com.kotlindiscord.kord.extensions.modules.extra.tags.config.TagsConfig
import com.kotlindiscord.kord.extensions.modules.extra.tags.data.TagsData
import com.kotlindiscord.kord.extensions.utils.loadModule
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
