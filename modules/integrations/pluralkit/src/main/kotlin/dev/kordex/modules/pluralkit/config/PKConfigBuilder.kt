/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

@file:Suppress("StringLiteralDuplication")

package dev.kordex.modules.pluralkit.config

import dev.kord.common.ratelimit.IntervalRateLimiter
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class PKConfigBuilder {
	/**
	 *  A mapping of domain names to their corresponding rate limiters.
	 *
	 *  Provide `null` for a domain to disable rate limiting.
	 */
	val domainRateLimiters: MutableMap<String, IntervalRateLimiter?> = mutableMapOf(
		"api.pluralkit.me" to IntervalRateLimiter(2, 1.seconds)
	)

	/**
	 * Rate limiter to use by default, when there's no domain-specific rate limiter.
	 *
	 * Provide `null` to disable rate limiting.
	 */
	var defaultRateLimiter: IntervalRateLimiter? = IntervalRateLimiter(2, 1.seconds)

	/** Replace the default rate limiter, using the specified settings. **/
	fun defaultLimit(limit: Int, interval: Duration) {
		defaultRateLimiter = IntervalRateLimiter(limit, interval)
	}

	/** Remove the default rate limiter, disabling rate limiting by default. **/
	fun unlimitByDefault() {
		defaultRateLimiter = null
	}

	/** Set a domain-specific rate limiter, using the specified settings. **/
	fun domainLimit(domain: String, limit: Int, interval: Duration) {
		if ("/" in domain) {
			error("URL provided as the `domain` parameter - please provide a domain instead.")
		}

		domainRateLimiters[domain] = IntervalRateLimiter(limit, interval)
	}

	/** Remove a domain-specific rate limiter, making it use the default rate limiter instead. **/
	fun defaultDomainLimit(domain: String) {
		if ("/" in domain) {
			error("URL provided as the `domain` parameter - please provide a domain instead.")
		}

		domainRateLimiters.remove(domain)
	}

	/** Disable rate limiting for the given domain. **/
	fun unlimitDomain(domain: String) {
		if ("/" in domain) {
			error("URL provided as the `domain` parameter - please provide a domain instead.")
		}

		domainRateLimiters[domain] = null
	}

	internal fun getLimiter(url: String): IntervalRateLimiter? {
		var domain = url

		if ("://" in domain) {
			domain = domain.split("://", limit = 2).first()
		}

		if ("/" in domain) {
			domain = domain.split("/", limit = 2).first()
		}

		if (domain in domainRateLimiters) {
			return domainRateLimiters[domain]
		}

		return defaultRateLimiter
	}
}
