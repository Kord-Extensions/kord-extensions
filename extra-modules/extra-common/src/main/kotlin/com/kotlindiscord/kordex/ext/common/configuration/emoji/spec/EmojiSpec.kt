package com.kotlindiscord.kordex.ext.common.configuration.emoji.spec

import com.uchuhimo.konf.ConfigSpec
import dev.kord.common.entity.Snowflake

/** @suppress **/
object EmojiSpec : ConfigSpec() {
    val guilds by required<List<Snowflake>>()
    val overrides by required<Map<String, Snowflake>>()
}
