package com.kotlindiscord.kord.extensions.utils

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.temporal.TemporalUnit
import java.util.concurrent.TimeUnit

/**
 * Class test for the extension of [Duration] class
 */
class TimeTest {

    /**
     * Test the transformation of [Duration] to the human view with [Duration.toHuman]
     */
    @Test
    fun `duration to human view`() {
        assertNull(Duration.ofMillis(1).toHuman())
        assertNull(Duration.ofSeconds(0).toHuman())
        assertEquals("1 second", Duration.ofSeconds(1).toHuman())
        assertEquals("2 seconds", Duration.ofSeconds(2).toHuman())
        assertEquals("1 minute", Duration.ofMinutes(1).toHuman())
        assertEquals("2 minutes", Duration.ofMinutes(2).toHuman())
        assertEquals("1 hour", Duration.ofHours(1).toHuman())
        assertEquals("2 hours", Duration.ofHours(2).toHuman())
        assertEquals("1 day", Duration.ofDays(1).toHuman())
        assertEquals("2 days", Duration.ofDays(2).toHuman())
        assertEquals("2 days, 5 hours, 1 minute", Duration
            .ofDays(2)
            .plusHours(5)
            .plusMinutes(1)
            .plusSeconds(0)
            .toHuman())
    }
}
