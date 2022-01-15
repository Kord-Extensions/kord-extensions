/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.modules.extra.mappings.arguments

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.converters.impl.stringChoice
import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalString
import com.kotlindiscord.kord.extensions.commands.converters.impl.string

/**
 * Arguments for class, field, and method conversion commands.
 */
@Suppress("UndocumentedPublicProperty")
class MappingConversionArguments(enabledNamespaces: Map<String, String>) : Arguments() {
    val query by string {
        name = "query"
        description = "Name to query mappings for"
    }

    val inputNamespace by stringChoice {
        name = "input"
        description = "The namespace to convert from"

        choices(enabledNamespaces)
    }

    val outputNamespace by stringChoice {
        name = "output"
        description = "The namespace to convert to"

        choices(enabledNamespaces)
    }

    val version by optionalString {
        name = "version"
        description = "Minecraft version to use for this query"
    }

    val inputChannel by optionalString {
        name = "inputChannel"
        description = "The mappings channel to use for input"
    }

    val outputChannel by optionalString {
        name = "outputChannel"
        description = "The mappings channel to use for output"
    }
}
