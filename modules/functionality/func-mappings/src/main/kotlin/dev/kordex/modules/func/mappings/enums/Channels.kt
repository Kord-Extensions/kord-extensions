/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.kordex.modules.func.mappings.enums

import dev.kordex.core.commands.application.slash.converters.ChoiceEnum

/**
 * Enum representing available Yarn channels.
 *
 * @property readableName String name used for the channel by Linkie
 */
enum class Channels(override val readableName: String) : ChoiceEnum {
	OFFICIAL("official"),
	SNAPSHOT("snapshot")
}
