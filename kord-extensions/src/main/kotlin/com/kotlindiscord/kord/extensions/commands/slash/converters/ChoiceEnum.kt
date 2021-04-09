package com.kotlindiscord.kord.extensions.commands.slash.converters

import com.kotlindiscord.kord.extensions.commands.slash.converters.impl.EnumChoiceConverter

/** Interface representing an enum used in the [EnumChoiceConverter]. **/
public interface ChoiceEnum {
    /** Human-readable name to show on Discord. **/
    public val readableName: String
}
