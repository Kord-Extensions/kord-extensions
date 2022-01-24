/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.modules.extra.mappings.arguments

import com.kotlindiscord.kord.extensions.commands.converters.impl.defaultingBoolean
import me.shedaniel.linkie.namespaces.QuiltMappingsNamespace

/**
 * Arguments for Quilt mapping lookup commands.
 */
@Suppress("UndocumentedPublicProperty")
class QuiltArguments : MappingArguments(QuiltMappingsNamespace), IntermediaryMappable {
    override val mapDescriptors by defaultingBoolean {
        name = "map-descriptor"
        description = "Whether to map field/method descriptors to named instead of hashed"
        defaultValue = true
    }
}
