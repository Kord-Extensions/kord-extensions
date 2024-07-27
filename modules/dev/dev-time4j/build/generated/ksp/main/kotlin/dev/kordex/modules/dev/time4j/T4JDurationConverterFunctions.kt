@file:OptIn(
	KordPreview::class,
	ConverterToDefaulting::class,
	ConverterToMulti::class,
	ConverterToOptional::class,
	UnexpectedFunctionBehaviour::class,
)

package dev.kordex.modules.dev.time4j

// Original converter class, for safety
import dev.kordex.modules.dev.time4j.T4JDurationConverter

// Imports that all converters need
import dev.kordex.core.InvalidArgumentException
import dev.kordex.core.annotations.UnexpectedFunctionBehaviour
import dev.kordex.core.commands.Arguments
import dev.kordex.core.commands.converters.*
import dev.kordex.core.commands.converters.builders.*
import dev.kord.common.annotation.KordPreview

// Converter type params
import net.time4j.Duration

// Extra imports
import net.time4j.*

/**
 * Builder class for t4JDuration converters. Used to construct a converter based on the given options.
 * 
 * @see T4JDurationConverter
 */
public class T4JDurationConverterBuilder( /** @inject: builderConstructorArguments **/ ) : ConverterBuilder<Duration<IsoUnit>>() {
    /** @inject: builderFields **/
    public var longHelp: Boolean = true

    init {
        /** @inject: builderInitStatements **/
    }

    /** @inject: builderExtraStatements **/

    public override fun build(arguments: Arguments): SingleConverter<Duration<IsoUnit>> {
        /** @inject: builderBuildFunctionPreStatements **/

        val converter = T4JDurationConverter(
            validator = validator,
            longHelp = longHelp,
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
 * Converter creation function: t4JDuration single converter
 * 
 * @see T4JDurationConverterBuilder
 */
public fun Arguments.t4JDuration(
    body: T4JDurationConverterBuilder.() -> Unit
): SingleConverter<Duration<IsoUnit>> {
    val builder = T4JDurationConverterBuilder( /** @inject: functionBuilderArguments **/ )
    
    body(builder)
    
    builder.validateArgument()
    
    return builder.build(this)
}

/**
 * Builder class for t4JDuration converters. Used to construct a converter based on the given options.
 * 
 * @see T4JDurationConverter
 */
public class DefaultingT4JDurationConverterBuilder( /** @inject: builderConstructorArguments **/ ) : DefaultingConverterBuilder<Duration<IsoUnit>>() {
    /** @inject: builderFields **/
    public var longHelp: Boolean = true

    init {
        /** @inject: builderInitStatements **/
    }

    /** @inject: builderExtraStatements **/

    public override fun build(arguments: Arguments): DefaultingConverter<Duration<IsoUnit>> {
        /** @inject: builderBuildFunctionPreStatements **/

        val converter = T4JDurationConverter(
            longHelp = longHelp,
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
 * Converter creation function: t4JDuration defaulting converter
 * 
 * @see DefaultingT4JDurationConverterBuilder
 */
public fun Arguments.defaultingT4JDuration(
    body: DefaultingT4JDurationConverterBuilder.() -> Unit
): DefaultingConverter<Duration<IsoUnit>> {
    val builder = DefaultingT4JDurationConverterBuilder( /** @inject: functionBuilderArguments **/ )
    
    body(builder)
    
    builder.validateArgument()
    
    return builder.build(this)
}

/**
 * Builder class for t4JDuration converters. Used to construct a converter based on the given options.
 * 
 * @see T4JDurationConverter
 */
public class OptionalT4JDurationConverterBuilder( /** @inject: builderConstructorArguments **/ ) : OptionalConverterBuilder<Duration<IsoUnit>>() {
    /** @inject: builderFields **/
    public var longHelp: Boolean = true

    init {
        /** @inject: builderInitStatements **/
    }

    /** @inject: builderExtraStatements **/

    public override fun build(arguments: Arguments): OptionalConverter<Duration<IsoUnit>> {
        /** @inject: builderBuildFunctionPreStatements **/

        val converter = T4JDurationConverter(
            longHelp = longHelp,
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
 * Converter creation function: t4JDuration optional converter
 * 
 * @see OptionalT4JDurationConverterBuilder
 */
public fun Arguments.optionalT4JDuration(
    body: OptionalT4JDurationConverterBuilder.() -> Unit
): OptionalConverter<Duration<IsoUnit>> {
    val builder = OptionalT4JDurationConverterBuilder( /** @inject: functionBuilderArguments **/ )
    
    body(builder)
    
    builder.validateArgument()
    
    return builder.build(this)
}
