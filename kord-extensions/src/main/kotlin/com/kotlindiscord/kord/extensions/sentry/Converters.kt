@file:OptIn(
    KordPreview::class,
    ConverterToDefaulting::class,
    ConverterToMulti::class,
    ConverterToOptional::class
)

package com.kotlindiscord.kord.extensions.sentry

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.*
import dev.kord.common.annotation.KordPreview
import io.sentry.protocol.SentryId

// TODO: Move to annotation

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
