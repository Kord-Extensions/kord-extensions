package com.kotlindiscord.kord.extensions.modules.extra.mappings.configuration.spec

import com.kotlindiscord.kord.extensions.modules.extra.mappings.enums.YarnChannels
import com.uchuhimo.konf.ConfigSpec

/** @suppress **/
object YarnSpec : ConfigSpec() {
    val channels by required<List<YarnChannels>>()
}
