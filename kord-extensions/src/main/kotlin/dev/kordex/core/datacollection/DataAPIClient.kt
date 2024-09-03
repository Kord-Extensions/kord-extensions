/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.datacollection

import dev.kordex.core.annotations.InternalAPI
import dev.kordex.data.api.DataCollection
import dev.kordex.data.api.DataEntity
import dev.kordex.data.api.serializers.DataCollectionSerializer
import dev.kordex.data.api.serializers.KXUUIDSerializer
import dev.kordex.data.api.types.Entity
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
import java.util.*

/** Base URL for the KordEx data collection API. Stats will be available on the site later. **/
@InternalAPI
public const val BASE_URL: String = "https://data.kordex.dev"

/** API client for the KordEx data collection API. **/
@InternalAPI
@Suppress("MagicNumber")
public object DataAPIClient {
	private val client = HttpClient {
		install(ContentNegotiation) {
			json(
				Json {
					serializersModule = SerializersModule {
						include(Json.serializersModule)

						contextual<DataCollection>(DataCollectionSerializer)
						contextual<UUID>(KXUUIDSerializer)
					}
				}
			)
		}
	}

	@Suppress("MagicNumber")
	private suspend fun httpDelete(url: String): HttpResponse {
		val response = client.delete(url)

		response.maybeThrow()

		return response
	}

	@Suppress("MagicNumber")
	private suspend fun httpGet(url: String): HttpResponse {
		val response = client.get(url)

		response.maybeThrow()

		return response
	}

	@Suppress("MagicNumber")
	private suspend inline fun <reified T> httpPost(url: String, body: T): HttpResponse {
		val response = client.post(url) {
			contentType(ContentType.Application.Json)
			setBody(body)
		}

		response.maybeThrow()

		return response
	}

	/**
	 * Delete a piece of collected data by providing its ID.
	 *
	 * @return true if the ID was valid, and the server deleted the data – false otherwise.
	 */
	@InternalAPI
	public suspend fun delete(uuid: UUID): Boolean {
		val response = httpDelete("$BASE_URL/data/$uuid")

		return response.status == HttpStatusCode.OK
	}

	/**
	 * Get a piece of collected data by providing its ID.
	 *
	 * @return The collected data corresponding with the given ID – null if it doesn't exist.
	 */
	@InternalAPI
	public suspend fun get(uuid: UUID): DataEntity? {
		val response = httpGet("$BASE_URL/data/$uuid")

		if (response.status != HttpStatusCode.OK) {
			return null
		}

		return response.body()
	}

	/**
	 * Submit a collected data entity for storage.
	 *
	 * If the entity contains an ID (and the ID is valid), this will replace the stored data for that ID.
	 * Otherwise, the server generates a new ID and stores the data under it.
	 *
	 * It is important to store this ID, as it allows you to retrieve and delete the data later.
	 *
	 * @return The ID corresponding with the stored data.
	 */
	@InternalAPI
	public suspend fun submit(data: Entity): UUID {
		val url = "$BASE_URL/" + when (data.metricType) {
			is DataCollection.Extra -> "data/extra"
			is DataCollection.Standard -> "data/standard"
			is DataCollection.Minimal -> "data/minimal"

			is DataCollection.None -> error("Cannot submit a None-typed data payload")
		}

		val response = httpPost(url, data)

		return UUID.fromString(response.bodyAsText())
	}

	private suspend fun HttpResponse.maybeThrow() {
		if (status == HttpStatusCode.NotFound) {
			return
		}

		if (status.value in 300..399) {
			throw RedirectResponseException(this, bodyAsText())
		}

		if (status.value in 400..499) {
			throw ClientRequestException(this, bodyAsText())
		}

		if (status.value in 500..599) {
			throw ServerResponseException(this, bodyAsText())
		}
	}
}
