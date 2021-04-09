package com.kotlindiscord.kord.extensions.test.bot

import com.kotlindiscord.kord.extensions.commands.slash.converters.ChoiceEnum

public enum class TestChoiceEnum(override val readableName: String) : ChoiceEnum {
    ONE("first"),
    TWO("second")
}
