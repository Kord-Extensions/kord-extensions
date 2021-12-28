/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.modules.extra.mappings.configuration.spec

import com.uchuhimo.konf.ConfigSpec

private const val DEFAULT_TIMEOUT = 300L

/** @suppress **/
object SettingsSpec : ConfigSpec() {
    /** @suppress **/
    val namespaces by required<List<String>>()

    /** @suppress **/
    val timeout by optional(DEFAULT_TIMEOUT)
}
