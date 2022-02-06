/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.modules.annotations

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.kotlindiscord.kord.extensions.modules.annotations.converters.ConverterProcessor
import org.koin.core.context.loadKoinModules
import org.koin.core.context.startKoin
import org.koin.dsl.bind
import org.koin.dsl.module

/**
 * Processor provider for the converter annotation processor.
 */
public class ConverterProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        startKoin {
            modules()
        }

        loadKoinModules(module { single { environment.logger } bind KSPLogger::class })

        return ConverterProcessor(
            environment.codeGenerator, environment.logger
        )
    }
}
