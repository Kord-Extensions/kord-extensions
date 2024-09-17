/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

@file:Suppress(
	"StringLiteralDuplication"
)

package dev.kordex.core.annotations.converters.builders

import org.koin.core.component.KoinComponent

/**
 * Builder class that itself builds converter builder functions. Not quite as meta as the other one.
 *
 * This is a fairly messy class containing a string builder, but what can you do?
 */
public class ConverterBuilderFunctionBuilder : KoinComponent {
	/** Comment to prepend to the function definition. **/
	public var comment: String? = null

	/** Builder class generic arguments. Omit the `<>`. **/
	public var builderGeneric: String? = null

	/** Builder function generic arguments. Omit the `<>`. **/
	public var functionGeneric: String? = null

	/** Extra generic bounds to place after `where` in the function signature. **/
	public var whereSuffix: String? = null

	internal val builderArguments: MutableList<String> = mutableListOf()

	/** Argument function name, `"Arguments.$name"`. **/
	public lateinit var name: String

	/** Builder class type, used as a receiver. **/
	public lateinit var builderType: String

	/** Argument type, the type the user should ultimately be given after parsing. **/
	public lateinit var argumentType: String

	/** Basic converter type, returned by the function. **/
	public lateinit var converterType: String

	/**
	 * Add a builder constructor argument. `name = value` only, please.
	 *
	 * If this is a lambda, you can refer to the outer `builder` to get values provided by the user.
	 */
	public fun builderArg(arg: String): Boolean =
		builderArguments.add(arg)

	/** Build the string that contains this builder's code. **/
	public fun build(): String {
		val builder = StringBuilder()

		if (comment != null) {
			builder.append("/**\n")

			comment!!.lines().forEach {
				builder.append(" * $it\n")
			}

			builder.append(" */\n")
		}

		builder.append("public ")

		if (functionGeneric != null) {
			builder.append("inline ")
		}

		builder.append("fun ")

		if (functionGeneric != null) {
			builder.append("<reified $functionGeneric> ")
		}

		builder.append("Arguments.$name(\n")

		builder.append("    body: $builderType")

		val splitBuilderGeneric = builderGeneric?.split(",")
			?.joinToString { it.split(":").first() }

		if (splitBuilderGeneric != null) {
			builder.append("<$splitBuilderGeneric>")
		}

		builder.append(".() -> Unit\n")

		// TODO: Arbitrary function arguments

		builder.append("): $converterType<$argumentType>")

		if (whereSuffix != null) {
			builder.append(" where $whereSuffix")
		}

		builder.append(" {\n")
		builder.append("    val builder = $builderType")

		if (splitBuilderGeneric != null) {
			builder.append("<$splitBuilderGeneric>")
		}

		builder.append("( /** @inject: functionBuilderArguments **/ ")

		if (builderArguments.isNotEmpty()) {
			builder.append("\n")

			builderArguments.forEach {
				builder.append("        $it,\n")
			}

			builder.append("    ")
		}

		builder.append(")\n")

		builder.append("    \n")
		builder.append("    body(builder)\n")
		builder.append("    \n")
		builder.append("    builder.validateArgument()\n")
		builder.append("    \n")
		builder.append("    return builder.build(this)\n")
		builder.append("}")

		return builder.toString()
	}
}

/** DSL function to easily build a converter builder class. Returns a String. **/
public fun builderFunction(body: ConverterBuilderFunctionBuilder.() -> Unit): String {
	val builder = ConverterBuilderFunctionBuilder()

	body(builder)

	return builder.build()
}
