package com.kotlindiscord.kord.extensions.test.bot

import com.kotlindiscord.kord.extensions.commands.application.slash.converters.ChoiceEnum

enum class TestChoiceEnum(override val readableName: String) : ChoiceEnum {
    ONE("first"),
    TWO("second")
}
