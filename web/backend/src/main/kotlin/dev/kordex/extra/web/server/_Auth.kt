/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.kordex.extra.web.server

import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*

public val oauthClient: HttpClient = HttpClient {
	install(ContentNegotiation) {
		json()
	}
}

public fun WebServer.configureAuth(app: Application) {
	app.install(Authentication) {
		oauth("oauth-discord") {
			urlProvider = {
				buildString {
					append("http")

					if (!config.devMode) {
						append("s")
					}

					append("://")
					append(config.hostname)
					append("/auth/callback")
				}
			}

			providerLookup = {
				OAuthServerSettings.OAuth2ServerSettings(
					name = "discord",
					authorizeUrl = "https://discord.com/oauth2/authorize",
					accessTokenUrl = "https://discord.com/api/oauth2/token",
					clientId = config.oauth.clientId,
					clientSecret = config.oauth.clientSecret,
					defaultScopes = listOf("guilds", "identify"),
					extraAuthParameters = listOf(),
				)
			}

			client = oauthClient
		}
	}
}
