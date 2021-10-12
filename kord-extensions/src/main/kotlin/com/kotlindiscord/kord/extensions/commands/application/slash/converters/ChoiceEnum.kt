package com.kotlindiscord.kord.extensions.commands.application.slash.converters

import com.kotlindiscord.kord.extensions.commands.application.slash.converters.impl.EnumChoiceConverter

/** Interface representing an enum used in the [EnumChoiceConverter]. **/
public interface ChoiceEnum {
    /** Human-readable name to show on Discord. **/
    public val readableName: String
}
