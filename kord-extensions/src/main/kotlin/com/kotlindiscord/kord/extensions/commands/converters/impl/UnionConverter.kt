package com.kotlindiscord.kord.extensions.commands.converters.impl

import com.kotlindiscord.kord.extensions.CommandException
import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.converters.*
import com.kotlindiscord.kord.extensions.commands.parser.Argument
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
    shouldThrow: Boolean = false
) : CoalescingConverter<Any>(shouldThrow) {
    override val signatureTypeString: String = typeName
        ?: converters.joinToString(" | ") { it.signatureTypeString }

    /** @suppress Internal validation function. **/
    public fun validate() {
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

    override suspend fun parse(args: List<String>, context: CommandContext, bot: ExtensibleBot): Int {
        for (converter in converters) {
            @Suppress("TooGenericExceptionCaught")
            when (converter) {
                is SingleConverter<*> -> try {
                    val result = converter.parse(args.first(), context, bot)

                    if (result) {
                        converter.parseSuccess = true
                        this.parsed = converter.parsed

                        return 1
                    }
                } catch (t: Throwable) {
                    if (shouldThrow) throw t
                }

                is DefaultingConverter<*> -> try {
                    val result = converter.parse(args.first(), context, bot)

                    if (result) {
                        converter.parseSuccess = true
                        this.parsed = converter.parsed

                        return 1
                    }
                } catch (t: Throwable) {
                    if (shouldThrow) throw t
                }

                is OptionalConverter<*> -> try {
                    val result = converter.parse(args.first(), context, bot)

                    if (result && converter.parsed != null) {
                        converter.parseSuccess = true
                        this.parsed = converter.parsed!!

                        return 1
                    }
                } catch (t: Throwable) {
                    if (shouldThrow) throw t
                }

                is MultiConverter<*> -> try {
                    val result = converter.parse(args, context, bot)

                    if (result > 0) {
                        converter.parseSuccess = true
                        this.parsed = converter.parsed

                        return result
                    }
                } catch (t: Throwable) {
                    if (shouldThrow) throw t
                }

                is CoalescingConverter<*> -> try {
                    val result = converter.parse(args, context, bot)

                    if (result > 0) {
                        converter.parseSuccess = true
                        this.parsed = converter.parsed

                        return result
                    }
                } catch (t: Throwable) {
                    if (shouldThrow) throw t
                }

                is DefaultingCoalescingConverter<*> -> try {
                    val result = converter.parse(args, context, bot)

                    if (result > 0) {
                        converter.parseSuccess = true
                        this.parsed = converter.parsed

                        return result
                    }
                } catch (t: Throwable) {
                    if (shouldThrow) throw t
                }

                is OptionalCoalescingConverter<*> -> try {
                    val result = converter.parse(args, context, bot)

                    if (result > 0 && converter.parsed != null) {
                        converter.parseSuccess = true
                        this.parsed = converter.parsed!!

                        return result
                    }
                } catch (t: Throwable) {
                    if (shouldThrow) throw t
                }

                else -> throw CommandException("Unknown converter type provided: $converter")
            }
        }

        return 0
    }

    override suspend fun toSlashOption(arg: Argument<*>): OptionsBuilder =
        StringChoiceBuilder(arg.displayName, arg.description).apply { required = true }
}
