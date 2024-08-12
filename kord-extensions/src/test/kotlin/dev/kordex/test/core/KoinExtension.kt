/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.test.core

import dev.kord.common.annotation.KordExperimental
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kordex.core.ExtensibleBot
import dev.kordex.core.koin.KordExContext
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.InvocationInterceptor

class KoinExtension : BeforeAllCallback, InvocationInterceptor, ExtensionContext.Store.CloseableResource {
	@OptIn(KordExperimental::class)
	override fun beforeAll(context: ExtensionContext?) = runBlocking {
		if (started) {
			return@runBlocking
		}

		bot = ExtensibleBot("") {
			kordBuilder = { token, _ ->
				// TODO: Use the builder when Kord makes it possible to

				Kord.restOnly(token) {
					applicationId = Snowflake.min
				}
			}
		}

		started = true
	}

	override fun close() {
		if (started) {
			// Should yeet everything in memory
			KordExContext.stopKoin()
			bot = null
		}
	}

	companion object {
		var started = false
		var bot: ExtensibleBot? = null
	}
}
