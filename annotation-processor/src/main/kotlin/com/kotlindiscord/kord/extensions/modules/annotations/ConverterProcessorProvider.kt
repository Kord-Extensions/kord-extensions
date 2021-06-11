package com.kotlindiscord.kord.extensions.modules.annotations

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.kotlindiscord.kord.extensions.modules.annotations.converters.ConverterProcessor

/**
 * Processor provider for the converter annotation processor.
 */
public class ConverterProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor = ConverterProcessor(
        environment.codeGenerator, environment.logger
    )
}
