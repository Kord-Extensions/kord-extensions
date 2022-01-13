/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

@file:Suppress(
    "StringLiteralDuplication"
)

package com.kotlindiscord.kord.extensions.modules.annotations.converters.builders

import com.kotlindiscord.kord.extensions.modules.annotations.containsAny
import com.kotlindiscord.kord.extensions.modules.annotations.converters.ConverterType
import org.koin.core.component.KoinComponent

/**
 * Builder class that itself builds converter builder classes. Meta, I know.
 *
 * This is a fairly messy class containing a string builder, but what can you do?
 */
public class ConverterBuilderClassBuilder : KoinComponent {
    /** Comment to prepend to the class definition. **/
    public var comment: String? = null

    /** Converter name - eg, `"${name}ConverterBuilder"`. **/
    public lateinit var name: String

    /**
     * Converter class - class that should be instantiated to construct the converter.
     *
     * Technically could be a function, but this would be playing with fire.
     */
    public lateinit var converterClass: String

    /** Argument type, the type the user should ultimately be given after parsing. **/
    public lateinit var argumentType: String

    /** Builder generic params, if any. Omit the `<>`. **/
    public var builderGeneric: String? = null

    internal val builderArguments: MutableList<String> = mutableListOf()
    internal val builderArgumentNames: MutableList<String> = mutableListOf()

    internal val builderFields: MutableList<String> = mutableListOf()
    internal val builderFieldNames: MutableList<String> = mutableListOf()

    internal val types: MutableSet<ConverterType> = mutableSetOf()

    internal var converterType: String = ""
    internal var builderType: String = ""

    /** The ultimate result, the final string created after calling [build]. **/
    public var result: String? = null
        private set

    /** Add a builder constructor argument. **/
    public fun builderArg(arg: String) {
        builderArguments.add(arg)
        builderArgumentNames.add(arg.split(":").first().split(" ").last())
    }

    /** Add a builder field. **/
    public fun builderField(field: String) {
        builderFields.add(field)
        builderFieldNames.add(field.split(":").first().split(" ").last())
    }

    /** Specify the converter types that this builder concerns. **/
    public fun types(vararg types: ConverterType) {
        this.types.addAll(types)
    }

    /** Build the string that contains this builder's code. It's also stored in [result]. **/
    public fun build(): String {
        builderType = ""
        converterType = ""

        val builder = StringBuilder()

        if (comment != null) {
            builder.append("/**\n")

            comment!!.lines().forEach {
                builder.append(" * $it\n")
            }

            builder.append(" */\n")
        }

        builder.append("public class ")

        types.groupBy { it.order }.forEach { (_, entries) ->
            if (entries.size > 1) {
                error("Only one of the following converter types may be specified at once: ${entries.joinToString()}")
            }
        }

        types.sortedBy { it.order }.forEach {
            if (it.appendFragment) {
                converterType += it.fragment
            }
        }

        builderType = "${converterType}${name}ConverterBuilder"

        builder.append(builderType)

        if (builderGeneric != null) {
            builder.append(" <$builderGeneric>")
        }

        builder.append("(")

        if (builderArguments.isNotEmpty()) {
            builder.append("\n")

            builderArguments.forEach {
                builder.append("    $it,\n")
            }
        }

        builder.append(") : ${converterType}ConverterBuilder<$argumentType>()")

        if (ConverterType.SINGLE in types && converterType.isEmpty()) {
            converterType += "Single"
        }

        converterType += "Converter"

        if (ConverterType.CHOICE in types) {
            builder.append(", ChoiceConverterBuilder<$argumentType>")
        }

        builder.append(" {\n")

        if (ConverterType.CHOICE in types) {
            builder.append("    override val choices = mutableMapOf()\n\n")
        }

        if (builderFields.isNotEmpty()) {
            builderFields.forEach {
                builder.append("    $it\n")
            }

            builder.append("\n")
        }

        builder.append("    public override fun build(arguments: Arguments): $converterType<$argumentType> {\n")
        builder.append("        return arguments.arg(\n")
        builder.append("            displayName = name,\n")
        builder.append("            description = description,\n")
        builder.append("\n")
        builder.append("            converter = $converterClass(\n")

        if (!types.containsAny(ConverterType.LIST, ConverterType.COALESCING, ConverterType.DEFAULTING)) {
            builder.append("                validator = validator,\n")
        }

        builderArgumentNames.forEach {
            builder.append("                $it = $it,\n")
        }

        builderFieldNames.forEach {
            builder.append("                $it = $it,\n")
        }

        builder.append("            )")

        if (types.contains(ConverterType.DEFAULTING)) {
            builder.append(".toDefaulting(\n")
            builder.append("                defaultValue = defaultValue,\n")
            builder.append("                outputError = !ignoreErrors,\n")
            builder.append("                nestedValidator = validator,\n")
            builder.append("            )")
        } else if (types.contains(ConverterType.OPTIONAL)) {
            builder.append(".toOptional(\n")
            builder.append("                outputError = !ignoreErrors,\n")
            builder.append("                nestedValidator = validator,\n")
            builder.append("            )")
        } else if (types.contains(ConverterType.LIST)) {
            builder.append(".toMulti(\n")
            builder.append("                required = !ignoreErrors,\n")
            builder.append("                nestedValidator = validator,\n")
            builder.append("            )")
        }

        builder.append("\n")
        builder.append("        )\n")
        builder.append("    }\n")
        builder.append("}\n")

        result = builder.toString()

        return result!!
    }
}

/** DSL function to easily build a converter builder class. Returns the builder. **/
public fun builderClass(body: ConverterBuilderClassBuilder.() -> Unit): ConverterBuilderClassBuilder {
    val builder = ConverterBuilderClassBuilder()

    body(builder)

    builder.build()

    return builder
}

/** @suppress TODO: Remove this later, it's for testing **/
public fun main() {
    println(
        builderClass {
            name = "Enum"
            converterClass = "EnumConverter"
            argumentType = "E"

            builderGeneric = "E: Enum<E>"

            builderArg("public var getter: suspend (String) -> E?")

            builderField("public lateinit var typeName: String")
            builderField("public var bundle: String? = null")

            types(ConverterType.COALESCING)
        }.result
    )
}
