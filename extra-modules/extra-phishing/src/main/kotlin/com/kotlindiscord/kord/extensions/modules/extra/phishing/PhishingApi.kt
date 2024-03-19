/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.modules.extra.phishing

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*

internal const val ALL_PATH = "https://phish.sinking.yachts/v2/all"
internal const val CHECK_PATH = "https://phish.sinking.yachts/v2/check/%"
internal const val RECENT_PATH = "https://phish.sinking.yachts/v2/recent/%"
internal const val SIZE_PATH = "https://phish.sinking.yachts/v2/dbsize"

/** Implementation of the Sinking Yachts phishing domain API. **/
class PhishingApi(private val userAgent: String) {
	private val client = HttpClient {
		install(ContentNegotiation) {
			json()
		}

		install(DefaultRequest) {
			header("User-Agent", userAgent)
		}

		install(WebSockets)

		expectSuccess = true
	}

	/** Get all known phishing domains from the API. **/
	suspend fun getAllDomains(): Set<String> =
		client.get(ALL_PATH).body()

	/** Query the API directly to check a specific domain. **/
	suspend fun checkDomain(domain: String): Boolean =
		client.get(CHECK_PATH.replace("%", domain)).body()

	/** Get all new phishing domains added in the previous [seconds] seconds. **/
	suspend fun getRecentDomains(seconds: Long): List<DomainChange> =
		client.get(RECENT_PATH.replace("%", seconds.toString())).body()

	/** Get the total number of phishing domains that the API knows about. **/
	suspend fun getTotalDomains(): Long =
		client.get(SIZE_PATH).body()

	/** Connect to the websocket and register a callback to receive changes. Returns a lifecycle wrapper. **/
	fun websocket(callback: suspend (DomainChange) -> Unit) =
		PhishingWebsocketWrapper(userAgent, callback)
}
