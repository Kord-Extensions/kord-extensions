@file:OptIn(
    KordPreview::class,
    ConverterToDefaulting::class,
    ConverterToMulti::class,
    ConverterToOptional::class
)

package com.kotlindiscord.kord.extensions.commands.converters.impl

import com.kotlindiscord.kord.extensions.CommandException
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.converters.*
import com.kotlindiscord.kord.extensions.commands.parser.Argument
import com.kotlindiscord.kord.extensions.commands.parser.Arguments
import dev.kord.common.annotation.KordPreview
import dev.kord.rest.builder.interaction.OptionsBuilder
import dev.kord.rest.builder.interaction.StringChoiceBuilder

/**
 * Experimental converter allowing for combining other converters together, with the caveat of type erasure.
 *
 * This converter does not support optional or defaulting converters.
 */
@OptIn(KordPreview::class)
public class UnionConverter(
    private val converters: Collection<Converter<*>>,

    typeName: String? = null,
    shouldThrow: Boolean = false,

    override var validator: (suspend Argument<*>.(Any) -> Unit)? = null
) : CoalescingConverter<Any>(shouldThrow) {
    override val signatureTypeString: String = typeName
        ?: converters.joinToString(" | ") { it.signatureTypeString }

    /** @suppress Internal validation function. **/
    public fun validateUnion() {
        val allConverters = converters.toMutableList()

        allConverters.removeLast()  // The last converter can be any type.

        for (converter in allConverters) {
            when (converter) {
                is DefaultingConverter<*>, is DefaultingCoalescingConverter<*> -> error(
                    "Invalid converter: $converter - " +
                        "Defaulting converters are only supported by union converters if they're the last " +
                        "provided converter."
                )

                is OptionalConverter<*>, is OptionalCoalescingConverter<*> -> error(
                    "Invalid converter: $converter - " +
                        "Optional converters are only supported by union converters if they're the last " +
                        "provided converter."
                )
            }
        }
    }

    override suspend fun parse(args: List<String>, context: CommandContext): Int {
        for (converter in converters) {
            @Suppress("TooGenericExceptionCaught")
            when (converter) {
                is SingleConverter<*> -> try {
                    val result = converter.parse(args.first(), context)

                    if (result) {
                        converter.parseSuccess = true
                        this.parsed = converter.parsed

                        return 1
                    }
                } catch (t: Throwable) {
                    if (shouldThrow) throw t
                }

                is DefaultingConverter<*> -> try {
                    val result = converter.parse(args.first(), context)

                    if (result) {
                        converter.parseSuccess = true
                        this.parsed = converter.parsed

                        return 1
                    }
                } catch (t: Throwable) {
                    if (shouldThrow) throw t
                }

                is OptionalConverter<*> -> try {
                    val result = converter.parse(args.first(), context)

                    if (result && converter.parsed != null) {
                        converter.parseSuccess = true
                        this.parsed = converter.parsed!!

                        return 1
                    }
                } catch (t: Throwable) {
                    if (shouldThrow) throw t
                }

                is MultiConverter<*> -> try {
                    val result = converter.parse(args, context)

                    if (result > 0) {
                        converter.parseSuccess = true
                        this.parsed = converter.parsed

                        return result
                    }
                } catch (t: Throwable) {
                    if (shouldThrow) throw t
                }

                is CoalescingConverter<*> -> try {
                    val result = converter.parse(args, context)

                    if (result > 0) {
                        converter.parseSuccess = true
                        this.parsed = converter.parsed

                        return result
                    }
                } catch (t: Throwable) {
                    if (shouldThrow) throw t
                }

                is DefaultingCoalescingConverter<*> -> try {
                    val result = converter.parse(args, context)

                    if (result > 0) {
                        converter.parseSuccess = true
                        this.parsed = converter.parsed

                        return result
                    }
                } catch (t: Throwable) {
                    if (shouldThrow) throw t
                }

                is OptionalCoalescingConverter<*> -> try {
                    val result = converter.parse(args, context)

                    if (result > 0 && converter.parsed != null) {
                        converter.parseSuccess = true
                        this.parsed = converter.parsed!!

                        return result
                    }
                } catch (t: Throwable) {
                    if (shouldThrow) throw t
                }

                else -> throw CommandException(
                    context.translate(
                        "converters.union.error.unknownConverterType",
                        replacements = arrayOf(converter)
                    )
                )
            }
        }

        return 0
    }

    override suspend fun toSlashOption(arg: Argument<*>): OptionsBuilder =
        StringChoiceBuilder(arg.displayName, arg.description).apply { required = true }
}

/**
 * Create a union converter, for combining other converters into a single argument - with the caveat of type erasure.
 *
 * This function will automatically remove converters if they were previously registered, so you can use pass it the
 * results of the usual extension functions.
 *
 * @see UnionConverter
 */
public fun Arguments.union(
    displayName: String,
    description: String,
    typeName: String? = null,
    shouldThrow: Boolean = false,
    vararg converters: Converter<*>,
    validator: (suspend Argument<*>.(Any) -> Unit)? = null,
): UnionConverter {
    val converter = UnionConverter(converters.toList(), typeName, shouldThrow, validator)

    converter.validateUnion()

    this.args.toList().forEach {
        if (it.converter in converters) {
            this.args.remove(it)
        }
    }

    arg(displayName, description, converter)

    return converter
}

/**
 * Create an optional union converter, for combining other converters into a single argument - with the caveat of
 * type erasure.
 *
 * This function will automatically remove converters if they were previously registered, so you can use pass it the
 * results of the usual extension functions.
 *
 * @see UnionConverter
 */
public fun Arguments.optionalUnion(
    displayName: String,
    description: String,
    typeName: String? = null,
    shouldThrow: Boolean = false,
    vararg converters: Converter<*>,
    validator: (suspend Argument<*>.(Any?) -> Unit)? = null
): OptionalCoalescingConverter<Any?> {
    val converter = UnionConverter(converters.toList(), typeName, shouldThrow)

    converter.validateUnion()

    this.args.toList().forEach {
        if (it.converter in converters) {
            this.args.remove(it)
        }
    }

    val optionalConverter = converter.toOptional(nestedValidator = validator)

    arg(displayName, description, optionalConverter)

    return optionalConverter
}
