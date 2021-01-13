package com.kotlindiscord.kord.extensions.utils

import dev.kord.rest.request.RestRequestException
import io.ktor.http.*

/**
 * Check if the [RestRequestException] has the same HTTP code of a [HttpStatusCode].
 * @receiver Exception from a request with rest interface.
 * @param status Status to compare the code of the exception.
 * @return `true` if there is one [HttpStatusCode] with the same HTTP code, `false` otherwise.
 */
public fun RestRequestException.hasStatus(vararg status: HttpStatusCode): Boolean {
    if (status.isEmpty()) return false
    val code = this.status.code
    return status.any { it.value == code }
}

/**
 * Check if the [RestRequestException] has the same HTTP code of a [HttpStatusCode].
 * @receiver Exception from a request with rest interface.
 * @param codes Status code to compare the code of the exception.
 * @return `true` if there is one [HttpStatusCode] with the same HTTP code, `false` otherwise.
 */
public fun RestRequestException.hasStatusCode(vararg codes: Int): Boolean {
    if (codes.isEmpty()) return false
    val code = this.status.code
    return codes.any { it == code }
}

/**
 * Check if the [RestRequestException] has not the same HTTP code of a [HttpStatusCode].
 * @receiver Exception from a request with rest interface.
 * @param status Status to compare the code of the exception.
 * @return `true` if there is none [HttpStatusCode] with the same HTTP code, `false` otherwise.
 */
public fun RestRequestException.hasNotStatus(vararg status: HttpStatusCode): Boolean =
    !hasStatus(*status)

/**
 * Check if the [RestRequestException] has not the same HTTP code of a [HttpStatusCode].
 * @receiver Exception from a request with rest interface.
 * @param codes Status code to compare the code of the exception.
 * @return `true` if there is none [HttpStatusCode] with the same HTTP code, `false` otherwise.
 */
public fun RestRequestException.hasNotStatusCode(vararg codes: Int): Boolean =
    !hasStatusCode(*codes)
