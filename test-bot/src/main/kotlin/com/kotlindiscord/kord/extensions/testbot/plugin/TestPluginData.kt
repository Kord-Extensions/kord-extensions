package com.kotlindiscord.kord.extensions.testbot.plugin

import com.kotlindiscord.kord.extensions.storage.Data
import kotlinx.serialization.Serializable
import net.peanuuutz.tomlkt.TomlComment

@Serializable
@Suppress("DataClassShouldBeImmutable")  // No.
public data class TestPluginData(
    @TomlComment("A test value. Nothing special here.")
    var key: String
) : Data
