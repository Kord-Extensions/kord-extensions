@file:OptIn(
	KordPreview::class,
	ConverterToDefaulting::class,
	ConverterToMulti::class,
	ConverterToOptional::class,
	UnexpectedFunctionBehaviour::class,
)

package dev.kordex.modules.func.mappings.converters

// Original converter class, for safety
import dev.kordex.modules.func.mappings.converters.MappingsVersionConverter

// Imports that all converters need
import dev.kordex.core.InvalidArgumentException
import dev.kordex.core.annotations.UnexpectedFunctionBehaviour
import dev.kordex.core.commands.Arguments
import dev.kordex.core.commands.converters.*
import dev.kordex.core.commands.converters.builders.*
import dev.kord.common.annotation.KordPreview

// Converter type params
import me.shedaniel.linkie.MappingsContainer

// Extra imports
import me.shedaniel.linkie.Namespace

/**
 * Builder class for mappingsVersion converters. Used to construct a converter based on the given options.
 * 
 * @see MappingsVersionConverter
 */
public class MappingsVersionConverterBuilder( /** @inject: builderConstructorArguments **/ ) : ConverterBuilder<MappingsContainer>() {
    /** @inject: builderFields **/
    public lateinit var namespaceGetter: suspend () -> Namespace

    init {
        /** @inject: builderInitStatements **/
    }

    /** @inject: builderExtraStatements **/
    /** Convenience function for setting the namespace getter to a specific namespace. **/
    public fun namespace(namespace: Namespace) {
        namespaceGetter = { namespace }
    }

    public override fun build(arguments: Arguments): SingleConverter<MappingsContainer> {
        /** @inject: builderBuildFunctionPreStatements **/

        val converter = MappingsVersionConverter(
            validator = validator,
            namespaceGetter = namespaceGetter,
        )

        /** @inject: builderBuildFunctionStatements **/

        return arguments.arg(
            displayName = name,
            description = description,

            converter = converter.withBuilder(this)
        )
    }

    override fun validateArgument() {
        super.validateArgument()

        if (!this::namespaceGetter.isInitialized) {
            throw InvalidArgumentException(this, "Required field not provided: namespaceGetter")
        }
    }
}

/**
 * Converter creation function: mappingsVersion single converter
 * 
 * @see MappingsVersionConverterBuilder
 */
public fun Arguments.mappingsVersion(
    body: MappingsVersionConverterBuilder.() -> Unit
): SingleConverter<MappingsContainer> {
    val builder = MappingsVersionConverterBuilder( /** @inject: functionBuilderArguments **/ )
    
    body(builder)
    
    builder.validateArgument()
    
    return builder.build(this)
}

/**
 * Builder class for mappingsVersion converters. Used to construct a converter based on the given options.
 * 
 * @see MappingsVersionConverter
 */
public class OptionalMappingsVersionConverterBuilder( /** @inject: builderConstructorArguments **/ ) : OptionalConverterBuilder<MappingsContainer>() {
    /** @inject: builderFields **/
    public lateinit var namespaceGetter: suspend () -> Namespace

    init {
        /** @inject: builderInitStatements **/
    }

    /** @inject: builderExtraStatements **/
    /** Convenience function for setting the namespace getter to a specific namespace. **/
    public fun namespace(namespace: Namespace) {
        namespaceGetter = { namespace }
    }

    public override fun build(arguments: Arguments): OptionalConverter<MappingsContainer> {
        /** @inject: builderBuildFunctionPreStatements **/

        val converter = MappingsVersionConverter(
            namespaceGetter = namespaceGetter,
        )

        /** @inject: builderBuildFunctionStatements **/

        return arguments.arg(
            displayName = name,
            description = description,

            converter = converter.toOptional(
                outputError = !ignoreErrors,
                nestedValidator = validator,
            ).withBuilder(this)
        )
    }

    override fun validateArgument() {
        super.validateArgument()

        if (!this::namespaceGetter.isInitialized) {
            throw InvalidArgumentException(this, "Required field not provided: namespaceGetter")
        }
    }
}

/**
 * Converter creation function: mappingsVersion optional converter
 * 
 * @see OptionalMappingsVersionConverterBuilder
 */
public fun Arguments.optionalMappingsVersion(
    body: OptionalMappingsVersionConverterBuilder.() -> Unit
): OptionalConverter<MappingsContainer> {
    val builder = OptionalMappingsVersionConverterBuilder( /** @inject: functionBuilderArguments **/ )
    
    body(builder)
    
    builder.validateArgument()
    
    return builder.build(this)
}
