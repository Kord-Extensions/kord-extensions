/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.utils

import dev.kord.rest.request.RestRequestException
import io.ktor.http.*

/**
 * Check if the given [RestRequestException] has a matching [HttpStatusCode].
 *
 * @receiver Exception object to check.
 * @param codes Status codes to check for.
 *
 * @return `true` if at least one status code matches, `false` otherwise.
 */
public fun RestRequestException.hasStatus(vararg codes: HttpStatusCode): Boolean {
	if (codes.isEmpty()) return false

	val code = this.status.code

	return codes.any { it.value == code }
}

/**
 * Check if the given [RestRequestException] has a matching integer status code.
 *
 * @receiver Exception object to check.
 * @param codes Status codes to check for.
 *
 * @return `true` if at least one status code matches, `false` otherwise.
 */
public fun RestRequestException.hasStatusCode(vararg codes: Int): Boolean {
	if (codes.isEmpty()) return false

	val code = this.status.code

	return codes.any { it == code }
}

/**
 * Check if the given [RestRequestException] **does not have** a matching [HttpStatusCode].
 *
 * @receiver Exception object to check.
 * @param codes Status codes to check for.
 *
 * @return `true` if **none of the status codes match**, `false` otherwise.
 */
public fun RestRequestException.hasNotStatus(vararg codes: HttpStatusCode): Boolean = !hasStatus(*codes)

/**
 * Check if the given [RestRequestException] **does not have** a matching integer status code.
 *
 * @receiver Exception object to check.
 * @param codes Status codes to check for.
 *
 * @return `true` if **none of the status codes match**, `false` otherwise.
 */
public fun RestRequestException.hasNotStatusCode(vararg codes: Int): Boolean = !hasStatusCode(*codes)
