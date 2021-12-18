/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.modules.extra.phishing

import io.ktor.client.*
import io.ktor.client.features.json.*
import io.ktor.client.request.*

internal const val ALL_PATH = "https://phish.sinking.yachts/v2/all"
internal const val CHECK_PATH = "https://phish.sinking.yachts/v2/check/%"
internal const val RECENT_PATH = "https://phish.sinking.yachts/v2/recent/%"
internal const val SIZE_PATH = "https://phish.sinking.yachts/v2/dbsize"

/** Implementation of the Sinking Yachts phishing domain API. **/
class PhishingApi(internal val appName: String) {
    internal val client = HttpClient {
        install(JsonFeature)
    }

    internal suspend inline fun <reified T> get(url: String): T = client.get(url) {
        header("X-Identity", "$appName (via Kord Extensions)")
    }

    /** Get all known phishing domains from the API. **/
    suspend fun getAllDomains(): Set<String> =
        get(ALL_PATH)

    /** Query the API directly to check a specific domain. **/
    suspend fun checkDomain(domain: String): Boolean =
        get(CHECK_PATH.replace("%", domain))

    /** Get all new phishing domains added in the previous [seconds] seconds. **/
    suspend fun getRecentDomains(seconds: Long): List<DomainChange> =
        get(RECENT_PATH.replace("%", seconds.toString()))

    /** Get the total number of phishing domains that the API knows about. **/
    suspend fun getTotalDomains(): Long =
        get(SIZE_PATH)
}
