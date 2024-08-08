/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.kordex.test.bot

import dev.kord.common.Locale
import dev.kord.common.entity.Snowflake
import dev.kord.gateway.ALL
import dev.kord.gateway.Intents
import dev.kord.gateway.PrivilegedIntent
import dev.kordex.core.ExtensibleBot
import dev.kordex.core.KORDEX_VERSION
import dev.kordex.core.checks.isNotBot
import dev.kordex.core.utils.env
import dev.kordex.core.utils.envOrNull
import dev.kordex.data.api.DataCollection
import dev.kordex.modules.func.mappings.extMappings
import dev.kordex.modules.func.phishing.extPhishing
import dev.kordex.modules.pluralkit.extPluralKit
import dev.kordex.modules.web.core.backend.utils.web
import dev.kordex.test.bot.extensions.*
import dev.kordex.test.bot.utils.LogLevel
import org.koin.core.logger.Level

public val TEST_SERVER_ID: Snowflake = Snowflake(env("TEST_SERVER"))

@OptIn(PrivilegedIntent::class)
public suspend fun main() {
	LogLevel.enabledLevel = LogLevel.fromString(envOrNull("LOG_LEVEL") ?: "INFO") ?: LogLevel.INFO

	val bot = ExtensibleBot(env("TOKEN")) {
		dataCollectionMode = DataCollection.Extra
		devMode = true
		koinLogLevel = Level.DEBUG

		about {
			name = "Test Bot"
			description = "Kord Extensions' official testing bot."
			logoUrl = "https://kordex.dev/logo.png"
			url = "https://kordex.dev"
			version = KORDEX_VERSION

			docsButton("https://docs.kordex.dev")
			donationButton("https://ko-fi.com/gsc")
			sourceButton("https://github.com/Kord-Extensions/kord-extensions")
		}

		chatCommands {
			enabled = true

			check { isNotBot() }
		}

		applicationCommands {
			defaultGuild(TEST_SERVER_ID)
		}

		intents {
			+Intents.ALL
		}

		i18n {
			interactionUserLocaleResolver()

			applicationCommandLocale(Locale.CHINESE_CHINA)
			applicationCommandLocale(Locale.ENGLISH_GREAT_BRITAIN)
			applicationCommandLocale(Locale.ENGLISH_UNITED_STATES)
			applicationCommandLocale(Locale.GERMAN)
			applicationCommandLocale(Locale.JAPANESE)
		}

		members {
			all()
		}

		extensions {
			web {
				hostname = "localhost:8080"
				siteTitle = "KordEx Testing"

				oauth {
					clientId = env("OAUTH_CLIENT_ID")
					clientSecret = env("OAUTH_CLIENT_SECRET")
				}
			}

			help {
				paginatorTimeout = 30
			}

			extPhishing {
				logChannelName = "alerts"
			}

			if (envOrNull("PLURALKIT_TESTING") != null) {
				extPluralKit()
			}

			if (envOrNull("MAPPINGS_TESTING") != null) {
				extMappings { }
			}

			add(::ArgumentTestExtension)
			add(::I18nTestExtension)
			add(::ModalTestExtension)
			add(::PaginatorTestExtension)
			add(::PKTestExtension)
			add(::SelectorTestExtension)
		}

		plugins {
			pluginPath("test-bot/plugins/")
			pluginPath("test-bot/build/generated/ksp/main/resources")
			pluginPath("modules/functionality/func-mappings/build/generated/ksp/main/resources")
		}
	}

	bot.start()
}
