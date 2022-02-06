/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.modules.extra.mappings.enums

import com.kotlindiscord.kord.extensions.commands.application.slash.converters.ChoiceEnum

/**
 * Enum representing available Yarn channels.
 *
 * @property str String name used for the channel by Linkie
 */
enum class Channels(override val readableName: String) : ChoiceEnum {
    OFFICIAL("official"),
    SNAPSHOT("snapshot")
}
