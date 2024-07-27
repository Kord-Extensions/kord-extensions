/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.kordex.modules.func.mappings.arguments

import dev.kordex.core.commands.application.slash.converters.ChoiceEnum
import me.shedaniel.linkie.Namespace

/** An argument base which provides an argument for a mapping channel. **/
@Suppress("UndocumentedPublicProperty")
abstract class MappingWithChannelArguments(namespace: Namespace) : MappingArguments(namespace) {
	abstract val channel: ChoiceEnum?
}
