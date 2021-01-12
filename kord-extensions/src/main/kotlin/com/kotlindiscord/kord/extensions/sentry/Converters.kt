package com.kotlindiscord.kord.extensions.sentry

import com.kotlindiscord.kord.extensions.commands.converters.MultiConverter
import com.kotlindiscord.kord.extensions.commands.converters.OptionalConverter
import com.kotlindiscord.kord.extensions.commands.converters.SingleConverter
import com.kotlindiscord.kord.extensions.commands.parser.Arguments
import io.sentry.protocol.SentryId

/**
 * Create a Sentry ID argument converter, for single arguments.
 *
 * @see SentryIdConverter
 */
public fun Arguments.sentryId(displayName: String, description: String): SingleConverter<SentryId> =
    arg(displayName, description, SentryIdConverter())

/**
 * Create an optional Sentry ID argument converter, for single arguments.
 *
 * @see SentryIdConverter
 */
public fun Arguments.optionalSentryId(displayName: String, description: String): OptionalConverter<SentryId?> =
    arg(displayName, description, SentryIdConverter().toOptional())

/**
 * Create a Sentry ID argument converter, for lists of arguments.
 *
 * @see SentryIdConverter
 */
public fun Arguments.sentryIdList(
    displayName: String,
    description: String,
    required: Boolean = true
): MultiConverter<SentryId> =
    arg(displayName, description, SentryIdConverter().toMulti(required))
