/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.modules.extra.mappings.configuration.spec

import com.kotlindiscord.kord.extensions.modules.extra.mappings.enums.YarnChannels
import com.uchuhimo.konf.ConfigSpec

/** @suppress **/
object YarnSpec : ConfigSpec() {
    /** @suppress **/
    val channels by required<List<YarnChannels>>()
}
