package com.kotlindiscord.kord.extensions

import com.kotlindiscord.kord.extensions.koin.KordExContext
import com.kotlindiscord.kord.extensions.utils.env
import com.kotlindiscord.kord.extensions.utils.envOrNull
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.InvocationInterceptor
import org.junit.jupiter.api.extension.ReflectiveInvocationContext
import java.lang.reflect.Method

class KoinExtension : BeforeAllCallback, InvocationInterceptor, ExtensionContext.Store.CloseableResource {
	override fun beforeAll(context: ExtensionContext?) = runBlocking {
		if (started) {
			return@runBlocking
		}

		val token = envOrNull("TOKEN")

		if (token != null) {
			// Needs to be fully set up for some tests
			bot = ExtensibleBot(env("TOKEN")) { }

			started = true
		}
	}

	override fun interceptTestMethod(
		invocation: InvocationInterceptor.Invocation<Void>,
		invocationContext: ReflectiveInvocationContext<Method>,
		extensionContext: ExtensionContext,
	) {
		if (envOrNull("TOKEN") == null) {
			logger.warn {
				"Skipping tests in ${invocationContext.targetClass.name} as no TOKEN env var was found."
			}

			invocation.skip()
		} else {
			invocation.proceed()
		}
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

		val logger = KotlinLogging.logger { }
	}
}
