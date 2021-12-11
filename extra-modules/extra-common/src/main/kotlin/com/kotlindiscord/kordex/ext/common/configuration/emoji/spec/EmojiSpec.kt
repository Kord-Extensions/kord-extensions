/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kordex.ext.common.configuration.emoji.spec

import com.uchuhimo.konf.ConfigSpec
import dev.kord.common.entity.Snowflake

/** @suppress **/
object EmojiSpec : ConfigSpec() {
    val guilds by required<List<Snowflake>>()
    val overrides by required<Map<String, Snowflake>>()
}
