@file:OptIn(
	KordPreview::class,
	ConverterToDefaulting::class,
	ConverterToMulti::class,
	ConverterToOptional::class,
	UnexpectedFunctionBehaviour::class,
)

package dev.kordex.modules.dev.java.time

// Original converter class, for safety
import dev.kordex.modules.dev.java.time.J8DurationCoalescingConverter

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
 * @see J8DurationCoalescingConverter
 */
public class CoalescingJ8DurationConverterBuilder( /** @inject: builderConstructorArguments **/ ) : CoalescingConverterBuilder<ChronoContainer>() {
    /** @inject: builderFields **/
    public var longHelp: Boolean = true
    public var positiveOnly: Boolean = true

    init {
        /** @inject: builderInitStatements **/
    }

    /** @inject: builderExtraStatements **/

    public override fun build(arguments: Arguments): CoalescingConverter<ChronoContainer> {
        /** @inject: builderBuildFunctionPreStatements **/

        val converter = J8DurationCoalescingConverter(
            validator = validator,
            shouldThrow = !ignoreErrors,
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
 * Converter creation function: j8Duration coalescing converter
 * 
 * @see CoalescingJ8DurationConverterBuilder
 */
public fun Arguments.coalescingJ8Duration(
    body: CoalescingJ8DurationConverterBuilder.() -> Unit
): CoalescingConverter<ChronoContainer> {
    val builder = CoalescingJ8DurationConverterBuilder( /** @inject: functionBuilderArguments **/ )
    
    body(builder)
    
    builder.validateArgument()
    
    return builder.build(this)
}

/**
 * Builder class for j8Duration converters. Used to construct a converter based on the given options.
 * 
 * @see J8DurationCoalescingConverter
 */
public class DefaultingCoalescingJ8DurationConverterBuilder( /** @inject: builderConstructorArguments **/ ) : DefaultingCoalescingConverterBuilder<ChronoContainer>() {
    /** @inject: builderFields **/
    public var longHelp: Boolean = true
    public var positiveOnly: Boolean = true

    init {
        /** @inject: builderInitStatements **/
    }

    /** @inject: builderExtraStatements **/

    public override fun build(arguments: Arguments): DefaultingCoalescingConverter<ChronoContainer> {
        /** @inject: builderBuildFunctionPreStatements **/

        val converter = J8DurationCoalescingConverter(
            shouldThrow = !ignoreErrors,
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
 * Converter creation function: j8Duration defaulting coalescing converter
 * 
 * @see DefaultingCoalescingJ8DurationConverterBuilder
 */
public fun Arguments.coalescingDefaultingJ8Duration(
    body: DefaultingCoalescingJ8DurationConverterBuilder.() -> Unit
): DefaultingCoalescingConverter<ChronoContainer> {
    val builder = DefaultingCoalescingJ8DurationConverterBuilder( /** @inject: functionBuilderArguments **/ )
    
    body(builder)
    
    builder.validateArgument()
    
    return builder.build(this)
}

/**
 * Builder class for j8Duration converters. Used to construct a converter based on the given options.
 * 
 * @see J8DurationCoalescingConverter
 */
public class OptionalCoalescingJ8DurationConverterBuilder( /** @inject: builderConstructorArguments **/ ) : OptionalCoalescingConverterBuilder<ChronoContainer>() {
    /** @inject: builderFields **/
    public var longHelp: Boolean = true
    public var positiveOnly: Boolean = true

    init {
        /** @inject: builderInitStatements **/
    }

    /** @inject: builderExtraStatements **/

    public override fun build(arguments: Arguments): OptionalCoalescingConverter<ChronoContainer> {
        /** @inject: builderBuildFunctionPreStatements **/

        val converter = J8DurationCoalescingConverter(
            shouldThrow = !ignoreErrors,
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
 * Converter creation function: j8Duration optional coalescing converter
 * 
 * @see OptionalCoalescingJ8DurationConverterBuilder
 */
public fun Arguments.coalescingOptionalJ8Duration(
    body: OptionalCoalescingJ8DurationConverterBuilder.() -> Unit
): OptionalCoalescingConverter<ChronoContainer> {
    val builder = OptionalCoalescingJ8DurationConverterBuilder( /** @inject: functionBuilderArguments **/ )
    
    body(builder)
    
    builder.validateArgument()
    
    return builder.build(this)
}
