package com.kotlindiscord.kord.extensions.modules.extra.mappings.configuration.spec

import com.uchuhimo.konf.ConfigSpec
import dev.kord.common.entity.Snowflake

/** @suppress **/
object CategoriesSpec : ConfigSpec() {
    val allowed by required<List<Snowflake>>()
    val banned by required<List<Snowflake>>()
}
