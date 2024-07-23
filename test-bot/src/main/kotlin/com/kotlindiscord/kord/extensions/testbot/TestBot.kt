/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.testbot

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.checks.isNotBot
import com.kotlindiscord.kord.extensions.modules.extra.mappings.extMappings
import com.kotlindiscord.kord.extensions.modules.extra.phishing.extPhishing
import com.kotlindiscord.kord.extensions.modules.extra.pluralkit.extPluralKit
import com.kotlindiscord.kord.extensions.testbot.extensions.*
import com.kotlindiscord.kord.extensions.testbot.utils.LogLevel
import com.kotlindiscord.kord.extensions.utils.env
import com.kotlindiscord.kord.extensions.utils.envOrNull
import dev.kord.common.Locale
import dev.kord.common.entity.Snowflake
import dev.kord.gateway.ALL
import dev.kord.gateway.Intents
import dev.kord.gateway.PrivilegedIntent
import dev.kordex.extra.web.utils.web
import org.koin.core.logger.Level

public val TEST_SERVER_ID: Snowflake = Snowflake(env("TEST_SERVER"))

@OptIn(PrivilegedIntent::class)
public suspend fun main() {
	LogLevel.enabledLevel = LogLevel.fromString(envOrNull("LOG_LEVEL") ?: "INFO") ?: LogLevel.INFO

	val bot = ExtensibleBot(env("TOKEN")) {
		koinLogLevel = Level.DEBUG

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
			add(::NestingTestExtension)
		}

		plugins {
			pluginPaths.clear()

			pluginPath("test-bot/build/generated/ksp/main/resources")
			pluginPath("extra-modules/extra-mappings/build/generated/ksp/main/resources")
		}
	}

	bot.start()
}
