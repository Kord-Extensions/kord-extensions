package com.kotlindiscord.kord.extensions.slash_commands.converters

import com.kotlindiscord.kord.extensions.commands.converters.SingleConverter
import dev.kord.common.annotation.KordPreview

@OptIn(KordPreview::class)
public abstract class ChoiceConverter<T : Any>(
    public open val choices: Map<String, T>
): SingleConverter<T>() {
    init {
        if (choices.size > 10) {
            error("You may not provide more than 10 choices.")
        }
    }
}
