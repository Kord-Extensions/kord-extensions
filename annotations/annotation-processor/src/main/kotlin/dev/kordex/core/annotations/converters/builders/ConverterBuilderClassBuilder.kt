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

import dev.kordex.core.annotations.containsAny
import dev.kordex.core.annotations.converters.ConverterType
import dev.kordex.core.annotations.converters.toCapitalized
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

	/** Extra generic bounds to place after `where` in the builder signature. **/
	public var whereSuffix: String? = null

	internal val builderArguments: MutableList<String> = mutableListOf()
	internal val builderArgumentNames: MutableList<String> = mutableListOf()

	internal val builderFields: MutableList<String> = mutableListOf()
	internal val builderFieldNames: MutableList<String> = mutableListOf()

	internal val builderBuildFunctionPreStatements: MutableList<String> = mutableListOf()
	internal val builderBuildFunctionStatements: MutableList<String> = mutableListOf()
	internal val builderExtraStatements: MutableList<String> = mutableListOf()
	internal val builderInitStatements: MutableList<String> = mutableListOf()

	internal val types: MutableSet<ConverterType> = mutableSetOf()

	internal var converterType: String = ""
	internal var functionSuffix: String = ""
	internal var builderType: String = ""

	/** The ultimate result, the final string created after calling [build]. **/
	public var result: String? = null
		private set

	/** Add a builder constructor argument. **/
	public fun builderArg(arg: String) {
		builderArguments.add(arg)

		if (!arg.startsWith("!!")) {
			builderArgumentNames.add(arg.split(":").first().split(" ").last())
		}
	}

	/** Add a builder field. **/
	public fun builderField(field: String) {
		builderFields.add(field)
		builderFieldNames.add(field.split(":").first().split(" ").last())
	}

	/** Add a builder build function statement. **/
	public fun builderBuildFunctionPreStatement(line: String) {
		builderBuildFunctionPreStatements.add(line)
	}

	/** Add a builder build function statement. **/
	public fun builderBuildFunctionStatement(line: String) {
		builderBuildFunctionStatements.add(line)
	}

	/** Add a builder extra statement. **/
	public fun builderExtraStatement(line: String) {
		builderExtraStatements.add(line)
	}

	/** Add a builder init statement. **/
	public fun builderInitStatement(line: String) {
		builderInitStatements.add(line)
	}

	/** Specify the converter types that this builder concerns. **/
	public fun types(vararg types: ConverterType) {
		types(types.toList())
	}

	/** Specify the converter types that this builder concerns. **/
	public fun types(types: Collection<ConverterType>) {
		this.types.addAll(types)
	}

	/** Build the string that contains this builder's code. It's also stored in [result]. **/
	public fun build(): String {
		builderType = ""

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

		converterType = buildString {
			types.sortedBy { it.order }.forEach {
				if (it.appendFragment) {
					append(it.fragment)
				}
			}

			builderType = this.toString() +
				name +

				if (ConverterType.CHOICE in types) {
					"Choice"
				} else {
					""
				} +

				"ConverterBuilder"

			builder.append(builderType)

			if (builderGeneric != null) {
				builder.append(" <$builderGeneric>")
			}

			builder.append("( /** @inject: builderConstructorArguments **/ ")

			if (builderArguments.isNotEmpty()) {
				builder.append("\n")

				builderArguments.forEach {
					builder.append("    ${it.trim('!', ' ')},\n")
				}
			}

			builder.append(") : ${this}ConverterBuilder<$argumentType>()")

			if (ConverterType.SINGLE in types && this.isEmpty()) {
				append("Single")
			}

			append("Converter")
		}

		if (ConverterType.CHOICE in types) {
			builder.append(", ChoiceConverterBuilder<$argumentType>")
		}

		if (whereSuffix != null) {
			builder.append(" where $whereSuffix")
		}

		builder.append(" {\n")

		if (ConverterType.CHOICE in types) {
			builder.append("    override var choices: MutableMap<Key, $argumentType> = mutableMapOf()\n\n")
		}

		builder.append("    /** @inject: builderFields **/\n")

		if (builderFields.isNotEmpty()) {
			builderFields.forEach {
				builder.append("    $it\n")
			}
		}

		builder.append("\n")

		builder.append("    init {\n")
		builder.append("        /** @inject: builderInitStatements **/\n")

		if (builderInitStatements.isNotEmpty()) {
			builderInitStatements.forEach {
				builder.append("        $it\n")
			}
		}

		builder.append("    }\n\n")
		builder.append("    /** @inject: builderExtraStatements **/\n")

		if (builderExtraStatements.isNotEmpty()) {
			builderExtraStatements.forEach {
				builder.append("    $it\n")
			}
		}

		builder.append("\n")

		builder.append("    public override fun build(arguments: Arguments): $converterType<$argumentType> {\n")
		builder.append("        /** @inject: builderBuildFunctionPreStatements **/\n\n")

		if (builderBuildFunctionPreStatements.isNotEmpty()) {
			builderBuildFunctionPreStatements.forEach {
				builder.append("        $it\n")
			}
		}

		builder.append("        val converter = $converterClass(\n")

		if (!types.containsAny(ConverterType.LIST, ConverterType.OPTIONAL, ConverterType.DEFAULTING)) {
			builder.append("            validator = validator,\n")
		}

		if (ConverterType.COALESCING in types) {
			builder.append("            shouldThrow = !ignoreErrors,\n")
		}

		if (ConverterType.CHOICE in types) {
			builder.append("            choices = choices,\n")
		}

		builderArgumentNames.forEach {
			builder.append("            $it = $it,\n")
		}

		builderFieldNames.forEach {
			builder.append("            $it = $it,\n")
		}

		builder.append("        )\n\n")
		builder.append("        /** @inject: builderBuildFunctionStatements **/\n")

		if (builderBuildFunctionStatements.isNotEmpty()) {
			builderBuildFunctionStatements.forEach {
				builder.append("        $it\n")
			}
		}

		builder.append("\n")

		builder.append("        return arguments.arg(\n")
		builder.append("            displayName = name,\n")
		builder.append("            description = description,\n")
		builder.append("\n")
		builder.append("            converter = converter")

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
			builder.append(".toList(\n")
			builder.append("                required = !ignoreErrors,\n")
			builder.append("                nestedValidator = validator,\n")
			builder.append("            )")
		}

		builder.append(".withBuilder(this)")

		builder.append("\n")
		builder.append("        )\n")
		builder.append("    }\n")

		val lateInit = builderFields
			.filter { it.contains("lateinit var") }
			.map { it.split(":").first() }
			.map { it.split(" ").last() }

		if (lateInit.isNotEmpty()) {
			builder.append("\n")

			builder.append("    override fun validateArgument() {\n")
			builder.append("        super.validateArgument()\n")

			lateInit.forEach {
				builder.append("\n")
				builder.append("        if (!this::$it.isInitialized) {\n")

				builder.append(
					"            throw InvalidArgumentException(this, " +
						"\"Required field not provided: $it\")\n"
				)

				builder.append("        }\n")
			}

			builder.append("    }\n")
		}

		builder.append("}\n")

		result = builder.toString()

		functionSuffix = converterType.removeSuffix("Converter")

		return result!!
	}

	internal fun getFunctionName(givenName: String): String {
		val before: MutableList<String> = mutableListOf()
		val after: MutableList<String> = mutableListOf()

		for (type in types) {
			when (type) {
				ConverterType.DEFAULTING -> before.add("defaulting")
				ConverterType.LIST -> after.add("list")
				ConverterType.OPTIONAL -> before.add("optional")
				ConverterType.COALESCING -> before.add("coalescing")
				ConverterType.CHOICE -> after.add("choice")

				ConverterType.SINGLE -> {
					/* Don't add anything */
				}
			}
		}

		val capitalizeName = before.isNotEmpty()
		var firstString = true

		val resultString = buildString {
			before.forEach {
				if (firstString) {
					append(it)
					firstString = false
				} else {
					append(it.toCapitalized())
				}
			}

			if (capitalizeName) {
				append(givenName.toCapitalized())
			} else {
				append(givenName)
			}

			after.forEach {
				append(it.toCapitalized())
			}
		}

		return resultString
	}
}

/** DSL function to easily build a converter builder class. Returns the builder. **/
public fun builderClass(body: ConverterBuilderClassBuilder.() -> Unit): ConverterBuilderClassBuilder {
	val builder = ConverterBuilderClassBuilder()

	body(builder)

	builder.build()

	return builder
}
