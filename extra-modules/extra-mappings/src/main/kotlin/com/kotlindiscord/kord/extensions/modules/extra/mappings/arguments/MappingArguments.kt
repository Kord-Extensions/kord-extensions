/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.modules.extra.mappings.arguments

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import com.kotlindiscord.kord.extensions.modules.extra.mappings.converters.optionalMappingsVersion
import me.shedaniel.linkie.Namespace

/**
 * Arguments base for mapping commands.
 */
@Suppress("UndocumentedPublicProperty")
open class MappingArguments(val namespace: Namespace) : Arguments() {
    val query by string {
        name = "query"
        description = "Name to query mappings for"
    }

    val version by optionalMappingsVersion {
        name = "version"
        description = "Minecraft version to use for this query"

        namespace(namespace)
    }
}
