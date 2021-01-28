package com.kotlindiscord.kord.extensions.commands.converters.impl

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.ParseException
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
    private val converters: Collection<Converter<Any>>,

    typeName: String,
    shouldThrow: Boolean = false
) : CoalescingConverter<Any>(shouldThrow) {
    override val signatureTypeString: String = typeName

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
            try {
                when (converter) {
                    is SingleConverter<*> -> {
                        val result = converter.parse(args.first(), context, bot)

                        if (result) {
                            converter.parseSuccess = true
                            this.parsed = converter.parsed

                            return 1
                        }
                    }

                    is DefaultingConverter<*> -> {
                        val result = converter.parse(args.first(), context, bot)

                        if (result) {
                            converter.parseSuccess = true
                            this.parsed = converter.parsed

                            return 1
                        }
                    }

                    is OptionalConverter<*> -> {
                        val result = converter.parse(args.first(), context, bot)

                        if (result && converter.parsed != null) {
                            converter.parseSuccess = true
                            this.parsed = converter.parsed!!

                            return 1
                        }
                    }

                    is MultiConverter<*> -> {
                        val result = converter.parse(args, context, bot)

                        if (result > 0) {
                            converter.parseSuccess = true
                            this.parsed = converter.parsed

                            return result
                        }
                    }

                    is CoalescingConverter<*> -> {
                        val result = converter.parse(args, context, bot)

                        if (result > 0) {
                            converter.parseSuccess = true
                            this.parsed = converter.parsed

                            return result
                        }
                    }

                    is DefaultingCoalescingConverter<*> -> {
                        val result = converter.parse(args, context, bot)

                        if (result > 0) {
                            converter.parseSuccess = true
                            this.parsed = converter.parsed

                            return result
                        }
                    }

                    is OptionalCoalescingConverter<*> -> {
                        val result = converter.parse(args, context, bot)

                        if (result > 0 && converter.parsed != null) {
                            converter.parseSuccess = true
                            this.parsed = converter.parsed!!

                            return result
                        }
                    }

                    else -> throw ParseException("Unknown converter type provided: $converter")
                }
            } catch (t: Throwable) {
                if (shouldThrow) throw t
            }
        }

        return 0
    }

    override suspend fun toSlashOption(arg: Argument<*>): OptionsBuilder =
        StringChoiceBuilder(arg.displayName, arg.description).apply { required = true }
}
