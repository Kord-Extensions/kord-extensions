package com.kotlindiscord.kord.extensions.modules.extra.phishing

import io.ktor.client.HttpClient
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.logging.LogLevel
import io.ktor.client.features.logging.Logging
import io.ktor.client.request.get
import io.ktor.client.request.header

internal const val ALL_PATH = "https://phish.sinking.yachts/all"
internal const val CHECK_PATH = "https://phish.sinking.yachts/check/%"
internal const val RECENT_PATH = "https://phish.sinking.yachts/recent/%"
internal const val SIZE_PATH = "https://phish.sinking.yachts/dbsize"

/** Implementation of the Sinking Yachts phishing domain API. **/
class PhishingApi(internal val appName: String) {
    internal val client = HttpClient {
        install(JsonFeature)
        install(Logging) {
            level = LogLevel.INFO
        }
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
    suspend fun getRecentDomains(seconds: Long): Set<String> =
        get(RECENT_PATH.replace("%", seconds.toString()))

    /** Get the total number of phishing domains that the API knows about. **/
    suspend fun getTotalDomains(): Long =
        get(SIZE_PATH)
}
