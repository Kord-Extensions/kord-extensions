package com.kotlindiscord.kord.extensions.sentry

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.ParseException
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.converters.SingleConverter
import io.sentry.protocol.SentryId

/**
 * Argument converter for Sentry event ID arguments.
 *
 * @see sentryId
 * @see sentryIdList
 */
public class SentryIdConverter : SingleConverter<SentryId>() {
    override val signatureTypeString: String = "uuid"

    override suspend fun parse(arg: String, context: CommandContext, bot: ExtensibleBot): Boolean {
        try {
            this.parsed = SentryId(arg)
        } catch (e: IllegalArgumentException) {
            throw ParseException("Invalid Sentry event ID specified: `$arg`")
        }

        return true
    }
}
