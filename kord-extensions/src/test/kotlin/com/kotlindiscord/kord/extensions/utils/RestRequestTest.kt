package com.kotlindiscord.kord.extensions.utils

import dev.kord.rest.request.HttpStatus
import dev.kord.rest.request.RestRequestException
import io.ktor.http.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * Tests for [RestRequestException] extension functions.
 */
class RestRequestTest {

    /**
     * Mock class implementing [RestRequestException].
     */
    private inner class RequestMockException(status: HttpStatus) : RestRequestException(status)

    /**
     * Test `hasStatus()` with zero parameters.
     */
    @Test
    fun `test hasStatus with zero parameters`() {
        createMockAndHasStatus(HttpStatusCode.Forbidden, false)
    }

    /**
     * Test `hasStatus()` with one parameter.
     */
    @Test
    fun `test hasStatus with one parameter`() {
        createMockAndHasStatus(HttpStatusCode.Forbidden, true, HttpStatusCode.Forbidden)
    }

    /**
     * Test `hasStatus()` with multiple parameters.
     */
    @Test
    fun `test hasStatus with multiple parameters`() {
        createMockAndHasStatus(
            HttpStatusCode.Forbidden,
            true,
            HttpStatusCode.BadRequest, HttpStatusCode.Forbidden, HttpStatusCode.Accepted
        )
    }

    /**
     * Test `hasNotStatus()` with zero parameters.
     */
    @Test
    fun `test hasNotStatus with zero parameters`() {
        createMockAndHasNotStatus(HttpStatusCode.Forbidden, true)
    }

    /**
     * Test `hasNotStatus()` with one parameter.
     */
    @Test
    fun `test hasNotStatus with one parameter`() {
        createMockAndHasNotStatus(HttpStatusCode.Forbidden, false, HttpStatusCode.Forbidden)
    }

    /**
     * Test `hasNotStatus()` with multiple parameters.
     */
    @Test
    fun `test hasNotStatus with multiple parameters`() {
        createMockAndHasNotStatus(
            HttpStatusCode.Forbidden,
            false,
            HttpStatusCode.BadRequest, HttpStatusCode.Forbidden, HttpStatusCode.Accepted
        )
    }

    /**
     * Create an instance of [RequestMockException] with the given [status] code, and check whether it has a matching
     * status from [statuses] via `hasStatus()` and `hasStatusCode()`.
     *
     * @param status HTTP status code to be passed to the mock object.
     * @param result The expected return value of `hasStatus()` and `hasStatusCode()`.
     * @param statuses Status codes to check for.
     */
    private fun createMockAndHasStatus(status: HttpStatusCode, result: Boolean, vararg statuses: HttpStatusCode) {
        val code = status.value
        val ex = RequestMockException(HttpStatus(code, ""))

        assertEquals(result, ex.hasStatus(*statuses))
        assertEquals(result, ex.hasStatusCode(*statuses.map { it.value }.toIntArray()))
    }

    /**
     * Create an instance of [RequestMockException] with the given [status] code, and check whether it
     * **does not have** a matching status from [statuses] via `hasNotStatus()` and `hasNotStatusCode()`.
     *
     * @param status HTTP status code to be passed to the mock object.
     * @param result The expected return value of `hasNotStatus()` and `hasNotStatusCode()`.
     * @param statuses Status codes to check for.
     */
    private fun createMockAndHasNotStatus(status: HttpStatusCode, result: Boolean, vararg statuses: HttpStatusCode) {
        val code = status.value
        val ex = RequestMockException(HttpStatus(code, ""))

        assertEquals(result, ex.hasNotStatus(*statuses))
        assertEquals(result, ex.hasNotStatusCode(*statuses.map { it.value }.toIntArray()))
    }
}
