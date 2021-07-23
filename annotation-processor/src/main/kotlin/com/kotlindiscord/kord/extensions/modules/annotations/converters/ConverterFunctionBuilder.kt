@file:Suppress("UndocumentedPublicFunction", "UndocumentedPublicProperty")

package com.kotlindiscord.kord.extensions.modules.annotations.converters

import com.google.devtools.ksp.processing.KSPLogger
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/** Convenience class for building converter functions. **/
public class ConverterFunctionBuilder(
    public val name: String,
) : KoinComponent {
    private val logger: KSPLogger by inject()

    private var comment: String? = null
    private val functionArgs: MutableList<String> = mutableListOf()

    private var generic: String? = null

    private lateinit var converterName: String
    private val converterArgs: MutableList<String> = mutableListOf()

    private var wrapperName: String? = null
    private val wrapperArgs: MutableList<String> = mutableListOf()

    private val lines: MutableList<String> = mutableListOf()
    private lateinit var returnType: String

    private var implicitReturn: Boolean = true

    public fun returnType(type: String): ConverterFunctionBuilder {
        returnType = type

        return this
    }

    public fun defaultFirstArgs(): ConverterFunctionBuilder {
        requiredFunArg("displayName", "String")
        requiredFunArg("description", "String")

        return this
    }

    public fun defaultLastArgs(typeParam: String): ConverterFunctionBuilder {
        if (generic != null) {
            optionalFunArg("noinline validator", "Validator<$typeParam>", "null")
        } else {
            optionalFunArg("validator", "Validator<$typeParam>", "null")
        }

        return this
    }

    public fun comment(lines: String): ConverterFunctionBuilder {
        comment = lines

        return this
    }

    public fun converter(name: String): ConverterFunctionBuilder {
        converterName = name

        return this
    }

    public fun converterArg(name: String): ConverterFunctionBuilder {
        converterArgs.add("$name = $name")

        return this
    }

    public fun converterArg(name: String, value: String): ConverterFunctionBuilder {
        converterArgs.add("$name = $value")

        return this
    }

    public fun rawGeneric(generic: String): ConverterFunctionBuilder {
        this.generic = generic

        return this
    }

    public fun generic(name: String, type: String): ConverterFunctionBuilder {
        generic = "$name : $type"

        return this
    }

    public fun wrapper(name: String): ConverterFunctionBuilder {
        wrapperName = name

        return this
    }

    public fun wrapperArg(name: String): ConverterFunctionBuilder {
        wrapperArgs.add("$name = $name")

        return this
    }

    public fun wrapperArg(name: String, value: String): ConverterFunctionBuilder {
        wrapperArgs.add("$name = $value")

        return this
    }

    public fun rawFunArg(line: String): ConverterFunctionBuilder {
        functionArgs.add(line.trimEnd(','))

        return this
    }

    public fun requiredFunArg(name: String, type: String): ConverterFunctionBuilder {
        functionArgs.add("$name: $type")

        return this
    }

    public fun optionalFunArg(name: String, type: String, default: String): ConverterFunctionBuilder {
        functionArgs.add("$name: $type = $default")

        return this
    }

    public fun explicitReturn(): ConverterFunctionBuilder {
        implicitReturn = false

        return this
    }

    public fun line(line: String): ConverterFunctionBuilder {
        lines.add(line)

        return this
    }

    public fun maybe(bool: Boolean, callback: ConverterFunctionBuilder.() -> Unit): ConverterFunctionBuilder {
        if (bool) {
            callback(this)
        }

        return this
    }

    public fun maybe(
        predicate: () -> Boolean,
        callback: ConverterFunctionBuilder.() -> Unit
    ): ConverterFunctionBuilder = maybe(predicate(), callback)

    public fun build(): String {
        var result = ""

        if (comment != null) {
            result += """
                |/**
                ${comment!!.split("\n").joinToString("\n") { "| * $it" }}
                | */
            """.trimMargin() + "\n"
        }

        result += "public "

        if (generic != null) {
            result += "inline "
        }

        result += "fun "

        if (generic != null) {
            result += "<reified $generic> "
        }

        result += "Arguments.$name(\n"
        result += functionArgs.joinToString("") { "    $it,\n" }
        result += "): $returnType "

        result += if (implicitReturn) {
            "="
        } else {
            "{"
        }

        result += "\n"

        if (lines.isNotEmpty()) {
            result += lines.joinToString("") { "    $it\n" }
            result += "\n"
        }

        result += "    arg(\n"
        result += "        displayName = displayName,\n"
        result += "        description = description,\n"
        result += "\n"
        result += "        converter = $converterName("

        if (converterArgs.isNotEmpty()) {
            result += "\n"
            result += converterArgs.joinToString("") { "            $it,\n" }
            result += "        "
        }

        result += ")"

        result += if (converterArgs.isNotEmpty() && wrapperName != null) {
            ""
        } else if (converterArgs.isEmpty() && wrapperName != null) {
            "\n            "
        } else {
            "\n"
        }

        if (wrapperName != null) {
            result += ".to${wrapperName!!.toCapitalized()}("

            logger.info("== Wrapper args ==\n    ${wrapperArgs.joinToString(", ") { "\"$it\"" }}\n")

            if (wrapperArgs.isNotEmpty()) {
                result += "\n"

                result += if (converterArgs.isNotEmpty()) {
                    wrapperArgs.joinToString("") { "            $it,\n" } +
                        "        "
                } else {
                    wrapperArgs.joinToString("") { "                $it,\n" } +
                        "            "
                }
            }

            result += ")\n"
        }

        result += "    )\n"

        if (!implicitReturn) {
            result += "}"
        }

        return result
    }
}

@Suppress("FunctionNaming")  // Factory function
public fun ConverterFunctionBuilder(
    name: String,
    body: ConverterFunctionBuilder.() -> Unit
): String {
    val builder = ConverterFunctionBuilder(name = name)

    body(builder)

    return builder.build()
}
