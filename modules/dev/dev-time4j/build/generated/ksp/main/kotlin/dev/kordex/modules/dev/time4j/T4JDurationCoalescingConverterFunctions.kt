@file:OptIn(
	KordPreview::class,
	ConverterToDefaulting::class,
	ConverterToMulti::class,
	ConverterToOptional::class,
	UnexpectedFunctionBehaviour::class,
)

package dev.kordex.modules.dev.time4j

// Original converter class, for safety
import dev.kordex.modules.dev.time4j.T4JDurationCoalescingConverter

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
 * @see T4JDurationCoalescingConverter
 */
public class CoalescingT4JDurationConverterBuilder( /** @inject: builderConstructorArguments **/ ) : CoalescingConverterBuilder<Duration<IsoUnit>>() {
    /** @inject: builderFields **/
    public var longHelp: Boolean = true

    init {
        /** @inject: builderInitStatements **/
    }

    /** @inject: builderExtraStatements **/

    public override fun build(arguments: Arguments): CoalescingConverter<Duration<IsoUnit>> {
        /** @inject: builderBuildFunctionPreStatements **/

        val converter = T4JDurationCoalescingConverter(
            validator = validator,
            shouldThrow = !ignoreErrors,
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
 * Converter creation function: t4JDuration coalescing converter
 * 
 * @see CoalescingT4JDurationConverterBuilder
 */
public fun Arguments.coalescingT4JDuration(
    body: CoalescingT4JDurationConverterBuilder.() -> Unit
): CoalescingConverter<Duration<IsoUnit>> {
    val builder = CoalescingT4JDurationConverterBuilder( /** @inject: functionBuilderArguments **/ )
    
    body(builder)
    
    builder.validateArgument()
    
    return builder.build(this)
}

/**
 * Builder class for t4JDuration converters. Used to construct a converter based on the given options.
 * 
 * @see T4JDurationCoalescingConverter
 */
public class DefaultingCoalescingT4JDurationConverterBuilder( /** @inject: builderConstructorArguments **/ ) : DefaultingCoalescingConverterBuilder<Duration<IsoUnit>>() {
    /** @inject: builderFields **/
    public var longHelp: Boolean = true

    init {
        /** @inject: builderInitStatements **/
    }

    /** @inject: builderExtraStatements **/

    public override fun build(arguments: Arguments): DefaultingCoalescingConverter<Duration<IsoUnit>> {
        /** @inject: builderBuildFunctionPreStatements **/

        val converter = T4JDurationCoalescingConverter(
            shouldThrow = !ignoreErrors,
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
 * Converter creation function: t4JDuration defaulting coalescing converter
 * 
 * @see DefaultingCoalescingT4JDurationConverterBuilder
 */
public fun Arguments.coalescingDefaultingT4JDuration(
    body: DefaultingCoalescingT4JDurationConverterBuilder.() -> Unit
): DefaultingCoalescingConverter<Duration<IsoUnit>> {
    val builder = DefaultingCoalescingT4JDurationConverterBuilder( /** @inject: functionBuilderArguments **/ )
    
    body(builder)
    
    builder.validateArgument()
    
    return builder.build(this)
}

/**
 * Builder class for t4JDuration converters. Used to construct a converter based on the given options.
 * 
 * @see T4JDurationCoalescingConverter
 */
public class OptionalCoalescingT4JDurationConverterBuilder( /** @inject: builderConstructorArguments **/ ) : OptionalCoalescingConverterBuilder<Duration<IsoUnit>>() {
    /** @inject: builderFields **/
    public var longHelp: Boolean = true

    init {
        /** @inject: builderInitStatements **/
    }

    /** @inject: builderExtraStatements **/

    public override fun build(arguments: Arguments): OptionalCoalescingConverter<Duration<IsoUnit>> {
        /** @inject: builderBuildFunctionPreStatements **/

        val converter = T4JDurationCoalescingConverter(
            shouldThrow = !ignoreErrors,
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
 * Converter creation function: t4JDuration optional coalescing converter
 * 
 * @see OptionalCoalescingT4JDurationConverterBuilder
 */
public fun Arguments.coalescingOptionalT4JDuration(
    body: OptionalCoalescingT4JDurationConverterBuilder.() -> Unit
): OptionalCoalescingConverter<Duration<IsoUnit>> {
    val builder = OptionalCoalescingT4JDurationConverterBuilder( /** @inject: functionBuilderArguments **/ )
    
    body(builder)
    
    builder.validateArgument()
    
    return builder.build(this)
}
