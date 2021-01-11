package com.kotlindiscord.kord.extensions.utils

import dev.kord.rest.request.HttpStatus
import dev.kord.rest.request.RestRequestException
import io.ktor.http.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

/**
 * Class test for the extension of [RestRequestException] class
 */
class RestRequestTest {

    /**
     * Mock to implement an instance of [RestRequestException]
     */
    private inner class RequestMockException(status: HttpStatus): RestRequestException(status)

    /**
     * Test to check if the status is found with zero parameters
     */
    @Test
    fun `request exception has status with zero parameters`(){
        createMockAndHasStatus(HttpStatusCode.Forbidden, false)
    }

    /**
     * Test to check if the status is found with one parameters
     */
    @Test
    fun `request exception has status with one parameters`(){
        createMockAndHasStatus(HttpStatusCode.Forbidden, true, HttpStatusCode.Forbidden)
    }

    /**
     * Test to check if the status is found with several parameters
     */
    @Test
    fun `request exception has status with several parameters`(){
        createMockAndHasStatus(HttpStatusCode.Forbidden,
            true,
            HttpStatusCode.BadRequest, HttpStatusCode.Forbidden, HttpStatusCode.Accepted
        )
    }

    /**
     * Test to check if the status is not found with zero parameters
     */
    @Test
    fun `request exception has not status with zero parameters`(){
        createMockAndHasNotStatus(HttpStatusCode.Forbidden, true)
    }

    /**
     * Test to check if the status is not found with one parameters
     */
    @Test
    fun `request exception has not status with one parameters`(){
        createMockAndHasNotStatus(HttpStatusCode.Forbidden, false, HttpStatusCode.Forbidden)
    }

    /**
     * Test to check if the status is not found with several parameters
     */
    @Test
    fun `request exception has not status with several parameters`(){
        createMockAndHasNotStatus(HttpStatusCode.Forbidden,
            false,
            HttpStatusCode.BadRequest, HttpStatusCode.Forbidden, HttpStatusCode.Accepted
        )
    }

    /**
     * Create an instance of [RequestMockException] with the value [status] and try to know if the exception has a status
     * from [codesTest]
     * @param status HTTP Code
     * @param result `true` if [codesTest] should be found in status, `false` otherwise
     * @param codesTest Codes which must have at least one element present in [RequestMockException]
     */
    private fun createMockAndHasStatus(status: HttpStatusCode, result:Boolean, vararg codesTest: HttpStatusCode) {
        val code = status.value
        val ex = RequestMockException(HttpStatus(code, ""))
        assertEquals(result, ex.hasStatus(*codesTest))
        assertEquals(result, ex.hasStatusCode(*codesTest.map { it.value }.toIntArray()))
    }

    /**
     * Create an instance of [RequestMockException] with the value [status] and try to know if the exception has not a status
     * from [codesTest]
     * @param status HTTP Code
     * @param result `true` if [codesTest] should be not found in status, `false` otherwise
     * @param codesTest Codes that must not be in [RequestMockException]
     */
    private fun createMockAndHasNotStatus(status: HttpStatusCode, result:Boolean, vararg codesTest: HttpStatusCode) {
        val code = status.value
        val ex = RequestMockException(HttpStatus(code, ""))
        assertEquals(result, ex.hasNotStatus(*codesTest))
        assertEquals(result, ex.hasNotStatusCode(*codesTest.map { it.value }.toIntArray()))
    }
}
