package com.kotlindiscord.kord.extensions.modules.extra.mappings.configuration.spec

import com.uchuhimo.konf.ConfigSpec

private const val DEFAULT_TIMEOUT = 300L

/** @suppress **/
object SettingsSpec : ConfigSpec() {
    val namespaces by required<List<String>>()
    val timeout by optional(DEFAULT_TIMEOUT)
}
