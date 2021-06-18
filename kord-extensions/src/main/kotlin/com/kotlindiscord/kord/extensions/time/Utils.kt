package com.kotlindiscord.kord.extensions.time

import kotlinx.datetime.Instant

/**
 * Format the given `Instant` to Discord's automatically-formatted timestamp format. This will return a String that
 * you can include in your messages, which Discord should automatically format for users based on their locale.
 */
public fun Instant.toDiscord(format: TimestampType): String = format.format(toEpochMilliseconds())
