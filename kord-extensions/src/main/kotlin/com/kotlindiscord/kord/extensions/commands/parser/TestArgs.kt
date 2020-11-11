package com.kotlindiscord.kord.extensions.commands.parser

import com.kotlindiscord.kord.extensions.commands.converters.StringConverter
import com.kotlindiscord.kord.extensions.commands.converters.StringListConverter

class TestArgs : Arguments() {
    val first by arg("first", StringConverter())
    val second by arg("second", StringListConverter())
}
