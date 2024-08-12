/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

@file:Suppress("UndocumentedPublicClass", "UndocumentedPublicFunction", "UndocumentedPublicProperty")

package dev.kordex.modules.pluralkit.api

import dev.kord.common.entity.Snowflake
import dev.kord.common.ratelimit.IntervalRateLimiter
import dev.kordex.modules.pluralkit.utils.LRUHashMap
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import kotlin.time.Duration.Companion.seconds

internal const val PK_API_VERSION = 2

class PluralKit(
	private val baseUrl: String = "https://api.pluralkit.me",
	private val rateLimiter: IntervalRateLimiter? = IntervalRateLimiter(2, 1.seconds),

	userAgent: String,

	cacheSize: Int = 10_000,
) {
	private val logger = KotlinLogging.logger { }

	private val messageUrl: String = "${this.baseUrl}/v$PK_API_VERSION/messages/{id}"
	private val messageCache: LRUHashMap<String, PKMessage> = LRUHashMap(cacheSize)

	private val client = HttpClient {
		install(ContentNegotiation) {
			json(
				Json { ignoreUnknownKeys = true },
				ContentType.Any
			)
		}

		install(DefaultRequest) {
			header("User-Agent", userAgent)
		}

		expectSuccess = true
	}

	suspend fun getMessage(id: Snowflake) =
		getMessage(id.toString())

	@Suppress("MagicNumber")
	suspend fun getMessage(id: String): PKMessage {
		val cachedMessage = messageCache[id]

		if (cachedMessage != null) {
			return cachedMessage
		}

		val url = messageUrl.replace("id" to id)

		try {
			rateLimiter?.consume()

			val result: PKMessage = client.get(url).body()
			messageCache[id] = result

			logger.debug { "/messages/$id -> 200" }

			return result
		} catch (e: ClientRequestException) {
			if (e.response.status.value in 400 until 600) {
				if (e.response.status.value == HttpStatusCode.NotFound.value) {
					logger.debug { "/messages/$id -> ${e.response.status}" }
				} else {
					logger.error(e) { "/messages/$id -> ${e.response.status}" }
				}
			}

			throw e
		}
	}

	suspend fun getMessageOrNull(id: Snowflake) =
		getMessageOrNull(id.toString())

	suspend fun getMessageOrNull(id: String): PKMessage? {
		try {
			return getMessage(id)
		} catch (e: ClientRequestException) {
			if (e.response.status.value != HttpStatusCode.NotFound.value) {
				throw e
			}
		}

		return null
	}

	private fun String.replace(vararg pairs: Pair<String, Any>): String {
		var result = this

		pairs.forEach { (k, v) ->
			result = result.replace("{$k}", v.toString())
		}

		return result
	}
}
