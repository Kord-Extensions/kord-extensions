/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.modules.web.core.backend.server

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
