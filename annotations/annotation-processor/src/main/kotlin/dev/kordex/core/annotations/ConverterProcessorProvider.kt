/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.annotations

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import dev.kordex.core.annotations.converters.ConverterProcessor
import org.koin.core.context.GlobalContext
import org.koin.core.context.loadKoinModules
import org.koin.core.context.startKoin
import org.koin.dsl.bind
import org.koin.dsl.module

/**
 * Processor provider for the converter annotation processor.
 */
public class ConverterProcessorProvider : SymbolProcessorProvider {
	override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
		if (GlobalContext.getOrNull() == null) {
			startKoin {
				modules()
			}
		}

		loadKoinModules(module { single { environment.logger } bind KSPLogger::class })

		return ConverterProcessor(
			environment.codeGenerator, environment.logger
		)
	}
}
