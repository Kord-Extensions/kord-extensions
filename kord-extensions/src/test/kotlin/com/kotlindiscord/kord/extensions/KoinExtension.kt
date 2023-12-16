package com.kotlindiscord.kord.extensions

import com.kotlindiscord.kord.extensions.koin.KordExContext
import dev.kord.common.annotation.KordExperimental
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
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
