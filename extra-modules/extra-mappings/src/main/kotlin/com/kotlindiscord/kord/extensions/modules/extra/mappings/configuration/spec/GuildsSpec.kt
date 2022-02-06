/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.modules.extra.mappings.configuration.spec

import com.uchuhimo.konf.ConfigSpec
import dev.kord.common.entity.Snowflake

/** @suppress **/
object GuildsSpec : ConfigSpec() {
    /** @suppress **/
    val allowed by required<List<Snowflake>>()

    /** @suppress **/
    val banned by required<List<Snowflake>>()
}
