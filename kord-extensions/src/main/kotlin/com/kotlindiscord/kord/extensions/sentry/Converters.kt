/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

@file:OptIn(
    ConverterToMulti::class,
    ConverterToOptional::class
)

package com.kotlindiscord.kord.extensions.sentry

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.*
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
public fun Arguments.optionalSentryId(displayName: String, description: String): OptionalConverter<SentryId> =
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
): ListConverter<SentryId> =
    arg(displayName, description, SentryIdConverter().toList(required))
