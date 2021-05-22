package com.kotlindiscord.kord.extensions.commands

import com.kotlindiscord.kord.extensions.annotations.ExtensionDSL
import com.kotlindiscord.kord.extensions.commands.parser.Arguments
import com.kotlindiscord.kord.extensions.extensions.Extension
import java.util.*

/**
 * Class representing a subcommand.
 *
 * This is used for group commands, so that subcommands are aware of their parent.
 *
 * @param extension The [Extension] that registered this command.
 * @param parent The [GroupCommand] this command exists under.
 */
@ExtensionDSL
public open class MessageSubCommand<T : Arguments>(
    extension: Extension,
    arguments: (() -> T)? = null,
    public open val parent: GroupCommand<out Arguments>
) : MessageCommand<T>(extension, arguments) {
    /** Get the full command name, translated, with parent commands taken into account. **/
    public open suspend fun getFullTranslatedName(locale: Locale): String =
        parent.getFullTranslatedName(locale) + " " + this.getTranslatedName(locale)
}
