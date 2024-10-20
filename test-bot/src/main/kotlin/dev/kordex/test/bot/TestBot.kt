/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.test.bot

import dev.kord.common.Locale
import dev.kord.common.entity.Snowflake
import dev.kord.gateway.ALL
import dev.kord.gateway.Intents
import dev.kord.gateway.PrivilegedIntent
import dev.kord.rest.builder.message.actionRow
import dev.kord.rest.builder.message.embed
import dev.kordex.core.DISCORD_BLURPLE
import dev.kordex.core.ExtensibleBot
import dev.kordex.core.KORDEX_VERSION
import dev.kordex.core.annotations.NotTranslated
import dev.kordex.core.builders.about.CopyrightType
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

@OptIn(PrivilegedIntent::class, NotTranslated::class)
public suspend fun main() {
	LogLevel.enabledLevel = LogLevel.fromString(envOrNull("LOG_LEVEL") ?: "INFO") ?: LogLevel.INFO

	val bot = ExtensibleBot(env("TOKEN")) {
		dataCollectionMode = DataCollection.Extra
		devMode = true
		koinLogLevel = Level.DEBUG

		about {
			ephemeral = false

			general {
				message { locale ->
					embed {
						color = DISCORD_BLURPLE
						title = "Test Bot"
						url = "https://kordex.dev"

						thumbnail {
							url = "https://kordex.dev/logo.png"
						}

						description = "Kord Extensions' official testing bot.\n\n" +
							"[Click here to learn more...](https://kordex.dev)"

						field {
							name = "Current Version"
							value = "`${KORDEX_VERSION}`"
						}
					}

					actionRow {
						linkButton("https://docs.kordex.dev") {
							label = "Docs"
						}

						linkButton("https://ko-fi.com/gsc") {
							label = "Donate"
						}

						linkButton("https://github.com/Kord-Extensions/kord-extensions") {
							label = "Source Code"
						}
					}
				}
			}

			section("banana", "banana") {
				message {
					content = "Banana!"
				}
			}

			copyright("Jansi", "Apache-2.0", CopyrightType.Library, "https://fusesource.github.io/jansi/")
			copyright("Logback", "EPL-1.0", CopyrightType.Library, "https://logback.qos.ch/")

			copyright(
				"Logback Groovy Config",
				"EPL-1.0",
				CopyrightType.Library,
				"https://github.com/virtualdogbert/logback-groovy-config"
			)
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
 			pluginPath("modules/functionality/func-mappings/build/generated/ksp/main/resources")
		}
	}

	bot.start()
}
