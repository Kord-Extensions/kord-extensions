/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

@file:Suppress("UndocumentedPublicFunction", "UndocumentedPublicProperty")

package dev.kordex.core.annotations.converters

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
		callback: ConverterFunctionBuilder.() -> Unit,
	): ConverterFunctionBuilder = maybe(predicate(), callback)

	public fun build(): String {
		val result = buildString {
			if (comment != null) {
				append(
					"""
                    |/**
                    ${comment!!.split("\n").joinToString("\n") { "| * $it" }}
                    | */
                    """.trimMargin() + "\n"
				)
			}

			append("public ")

			if (generic != null) {
				append("inline ")
			}

			append("fun ")

			if (generic != null) {
				append("<reified $generic> ")
			}

			append("Arguments.$name(\n")
			append(functionArgs.joinToString("") { "    $it,\n" })
			append("): $returnType ")

			if (implicitReturn) {
				append("=")
			} else {
				append("{")
			}

			append("\n")

			if (lines.isNotEmpty()) {
				append(lines.joinToString("") { "    $it\n" })
				append("\n")
			}

			append("    arg(\n")
			append("        displayName = displayName,\n")
			append("        description = description,\n")
			append("\n")
			append("        converter = $converterName(")

			if (converterArgs.isNotEmpty()) {
				append("\n")
				append(converterArgs.joinToString("") { "            $it,\n" })
				append("        ")
			}

			append(")")

			if (converterArgs.isNotEmpty() && wrapperName != null) {
				append("")
			} else if (converterArgs.isEmpty() && wrapperName != null) {
				append("\n            ")
			} else {
				append("\n")
			}

			if (wrapperName != null) {
				append(".to${wrapperName!!.toCapitalized()}(")

				logger.info("== Wrapper args ==\n    ${wrapperArgs.joinToString(", ") { "\"$it\"" }}\n")

				if (wrapperArgs.isNotEmpty()) {
					append("\n")

					if (converterArgs.isNotEmpty()) {
						append(
							wrapperArgs.joinToString("") { "            $it,\n" } +
								"        "
						)
					} else {
						append(
							wrapperArgs.joinToString("") { "                $it,\n" } +
								"            "
						)
					}
				}

				append(")\n")
			}

			append("    )\n")

			if (!implicitReturn) {
				append("}")
			}
		}

		return result
	}
}

@Suppress("FunctionNaming")  // Factory function
public fun ConverterFunctionBuilder(
	name: String,
	body: ConverterFunctionBuilder.() -> Unit,
): String {
	val builder = ConverterFunctionBuilder(name = name)

	body(builder)

	return builder.build()
}
