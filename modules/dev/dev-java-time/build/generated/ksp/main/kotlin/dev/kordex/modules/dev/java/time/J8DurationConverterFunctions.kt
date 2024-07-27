@file:OptIn(
	KordPreview::class,
	ConverterToDefaulting::class,
	ConverterToMulti::class,
	ConverterToOptional::class,
	UnexpectedFunctionBehaviour::class,
)

package dev.kordex.modules.dev.java.time

// Original converter class, for safety
import dev.kordex.modules.dev.java.time.J8DurationConverter

// Imports that all converters need
import dev.kordex.core.InvalidArgumentException
import dev.kordex.core.annotations.UnexpectedFunctionBehaviour
import dev.kordex.core.commands.Arguments
import dev.kordex.core.commands.converters.*
import dev.kordex.core.commands.converters.builders.*
import dev.kord.common.annotation.KordPreview

// Converter type params
import dev.kordex.modules.dev.java.time.ChronoContainer

// Extra imports
import java.time.*

/**
 * Builder class for j8Duration converters. Used to construct a converter based on the given options.
 * 
 * @see J8DurationConverter
 */
public class J8DurationConverterBuilder( /** @inject: builderConstructorArguments **/ ) : ConverterBuilder<ChronoContainer>() {
    /** @inject: builderFields **/
    public var longHelp: Boolean = true
    public var positiveOnly: Boolean = true

    init {
        /** @inject: builderInitStatements **/
    }

    /** @inject: builderExtraStatements **/

    public override fun build(arguments: Arguments): SingleConverter<ChronoContainer> {
        /** @inject: builderBuildFunctionPreStatements **/

        val converter = J8DurationConverter(
            validator = validator,
            longHelp = longHelp,
            positiveOnly = positiveOnly,
        )

        /** @inject: builderBuildFunctionStatements **/

        return arguments.arg(
            displayName = name,
            description = description,

            converter = converter.withBuilder(this)
        )
    }
}

/**
 * Converter creation function: j8Duration single converter
 * 
 * @see J8DurationConverterBuilder
 */
public fun Arguments.j8Duration(
    body: J8DurationConverterBuilder.() -> Unit
): SingleConverter<ChronoContainer> {
    val builder = J8DurationConverterBuilder( /** @inject: functionBuilderArguments **/ )
    
    body(builder)
    
    builder.validateArgument()
    
    return builder.build(this)
}

/**
 * Builder class for j8Duration converters. Used to construct a converter based on the given options.
 * 
 * @see J8DurationConverter
 */
public class DefaultingJ8DurationConverterBuilder( /** @inject: builderConstructorArguments **/ ) : DefaultingConverterBuilder<ChronoContainer>() {
    /** @inject: builderFields **/
    public var longHelp: Boolean = true
    public var positiveOnly: Boolean = true

    init {
        /** @inject: builderInitStatements **/
    }

    /** @inject: builderExtraStatements **/

    public override fun build(arguments: Arguments): DefaultingConverter<ChronoContainer> {
        /** @inject: builderBuildFunctionPreStatements **/

        val converter = J8DurationConverter(
            longHelp = longHelp,
            positiveOnly = positiveOnly,
        )

        /** @inject: builderBuildFunctionStatements **/

        return arguments.arg(
            displayName = name,
            description = description,

            converter = converter.toDefaulting(
                defaultValue = defaultValue,
                outputError = !ignoreErrors,
                nestedValidator = validator,
            ).withBuilder(this)
        )
    }
}

/**
 * Converter creation function: j8Duration defaulting converter
 * 
 * @see DefaultingJ8DurationConverterBuilder
 */
public fun Arguments.defaultingJ8Duration(
    body: DefaultingJ8DurationConverterBuilder.() -> Unit
): DefaultingConverter<ChronoContainer> {
    val builder = DefaultingJ8DurationConverterBuilder( /** @inject: functionBuilderArguments **/ )
    
    body(builder)
    
    builder.validateArgument()
    
    return builder.build(this)
}

/**
 * Builder class for j8Duration converters. Used to construct a converter based on the given options.
 * 
 * @see J8DurationConverter
 */
public class OptionalJ8DurationConverterBuilder( /** @inject: builderConstructorArguments **/ ) : OptionalConverterBuilder<ChronoContainer>() {
    /** @inject: builderFields **/
    public var longHelp: Boolean = true
    public var positiveOnly: Boolean = true

    init {
        /** @inject: builderInitStatements **/
    }

    /** @inject: builderExtraStatements **/

    public override fun build(arguments: Arguments): OptionalConverter<ChronoContainer> {
        /** @inject: builderBuildFunctionPreStatements **/

        val converter = J8DurationConverter(
            longHelp = longHelp,
            positiveOnly = positiveOnly,
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
}

/**
 * Converter creation function: j8Duration optional converter
 * 
 * @see OptionalJ8DurationConverterBuilder
 */
public fun Arguments.optionalJ8Duration(
    body: OptionalJ8DurationConverterBuilder.() -> Unit
): OptionalConverter<ChronoContainer> {
    val builder = OptionalJ8DurationConverterBuilder( /** @inject: functionBuilderArguments **/ )
    
    body(builder)
    
    builder.validateArgument()
    
    return builder.build(this)
}
