/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.test.modules.dev.java.time

import dev.kord.common.entity.Snowflake
import dev.kordex.core.ExtensibleBot
import dev.kordex.core.i18n.SupportedLocales
import dev.kordex.core.utils.env
import org.koin.core.logger.Level

val TEST_SERVER_ID = Snowflake(787452339908116521UL)

suspend fun main() {
	val bot = ExtensibleBot(env("TOKEN")) {
		koinLogLevel = Level.DEBUG

		i18n {
			localeResolver { _, _, user, _ ->
				@Suppress("UnderscoresInNumericLiterals")
				when (user?.id?.value) {
					560515299388948500UL -> SupportedLocales.FINNISH
					242043299022635020UL -> SupportedLocales.FRENCH
					407110650217627658UL -> SupportedLocales.FRENCH
					667552017434017794UL -> SupportedLocales.CHINESE_SIMPLIFIED
					185461862878543872UL -> SupportedLocales.GERMAN

					else -> defaultLocale
				}
			}
		}

		chatCommands {
			defaultPrefix = "?"

			prefix { default ->
				if (guildId == TEST_SERVER_ID) {
					"!"
				} else {
					default  // "?"
				}
			}
		}

		extensions {
			add(::TestExtension)
		}
	}

	bot.start()
}
