package com.kotlindiscord.kord.extensions.commands.slash.converters

import com.kotlindiscord.kord.extensions.commands.converters.SingleConverter
import dev.kord.common.annotation.KordPreview

private const val CHOICE_LIMIT = 25  // Discord doesn't allow more choices than this

/**
 * Special [SingleConverter] designed for slash commands, allowing you to specify up to 10 choices for the user.
 *
 * @property choices List of choices for the user to pick from.
 */
@OptIn(KordPreview::class)
public abstract class ChoiceConverter<T : Any>(
    public open val choices: Map<String, T>
) : SingleConverter<T>() {
    init {
        if (choices.size > CHOICE_LIMIT) {
            error("You may not provide more than $CHOICE_LIMIT choices.")
        }
    }
}
