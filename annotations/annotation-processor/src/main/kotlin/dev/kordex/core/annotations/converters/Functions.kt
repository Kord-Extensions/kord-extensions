/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

@file:Suppress("StringLiteralDuplication", "UnusedPrivateMember", "UNUSED_PARAMETER")

package dev.kordex.core.annotations.converters

import com.google.devtools.ksp.symbol.KSClassDeclaration

internal fun defaultingConverter(
	classDeclaration: KSClassDeclaration,
	name: String,
	typeParam: String,
	extraArguments: ArrayList<String>,
	generic: String?,
): String = ConverterFunctionBuilder(
	"defaulting${name.toCapitalized()}"
).comment(
	"""
       Creates a defaulting $name converter, for single arguments.

       @param defaultValue Default value to use if no argument was provided.
       @see ${classDeclaration.simpleName.asString()}
    """.trimIndent()
)
	.converter(classDeclaration.simpleName.asString())
	.returnType("DefaultingConverter<$typeParam>")
	.maybe(generic != null) {
		rawGeneric(generic!!)

		returnType(
			"DefaultingConverter<${generic.split(":").first().trim()}>"
		)
	}
	.defaultFirstArgs()
	.requiredFunArg("defaultValue", typeParam)
	.optionalFunArg("required", "Boolean", "false")
	.maybe(extraArguments.isNotEmpty()) {
		extraArguments.forEach {
			rawFunArg(it)
		}
	}
	.maybe(generic != null) {
		defaultLastArgs(generic!!.split(":").first().trim())
	}.maybe(generic == null) {
		defaultLastArgs(typeParam)
	}
	.maybe(extraArguments.isNotEmpty()) {
		extraArguments.forEach {
			converterArg(it.split(":").first().trim().split(" ").last())
		}
	}
	.wrapper("defaulting")
	.wrapperArg("defaultValue")
	.wrapperArg("outputError", "required")
	.wrapperArg("nestedValidator", "validator")
	.build()

internal fun defaultingChoiceConverter(
	classDeclaration: KSClassDeclaration,
	name: String,
	typeParam: String,
	extraArguments: ArrayList<String>,
	generic: String?,
): String = ConverterFunctionBuilder(
	"defaulting${name.toCapitalized()}Choice",
).comment(
	"""
       Creates a defaulting $name choice converter, for a defined set of single arguments.

       @param defaultValue Default value to use if no argument was provided.
       @see ${classDeclaration.simpleName.asString()}
    """.trimIndent()
)
	.converter(classDeclaration.simpleName.asString())
	.returnType("DefaultingConverter<$typeParam>")
	.maybe(generic != null) {
		rawGeneric(generic!!)

		returnType(
			"DefaultingConverter<<${generic.split(":").first().trim()}>>"
		)
	}
	.defaultFirstArgs()
	.requiredFunArg("defaultValue", typeParam)
	.optionalFunArg("required", "Boolean", "false")
	.requiredFunArg("choices", "Map<String, $typeParam>")
	.maybe(extraArguments.isNotEmpty()) {
		extraArguments.forEach {
			rawFunArg(it)
		}
	}
	.maybe(generic != null) {
		defaultLastArgs(generic!!.split(":").first().trim())
	}.maybe(generic == null) {
		defaultLastArgs(typeParam)
	}
	.converterArg("choices")
	.maybe(extraArguments.isNotEmpty()) {
		extraArguments.forEach {
			converterArg(it.split(":").first().trim().split(" ").last())
		}
	}
	.wrapper("defaulting")
	.wrapperArg("defaultValue")
	.wrapperArg("outputError", "required")
	.wrapperArg("nestedValidator", "validator")
	.build()

internal fun defaultingCoalescingConverter(
	classDeclaration: KSClassDeclaration,
	name: String,
	typeParam: String,
	extraArguments: ArrayList<String>,
	generic: String?,
): String = ConverterFunctionBuilder(
	"defaultingCoalescing${name.toCapitalized()}",
).comment(
	"""
       Creates a defaulting coalescing $name converter.

       @param defaultValue Default value to use if no argument was provided.
       @see ${classDeclaration.simpleName.asString()}
    """.trimIndent()
)
	.converter(classDeclaration.simpleName.asString())
	.returnType("DefaultingCoalescingConverter<$typeParam>")
	.maybe(generic != null) {
		rawGeneric(generic!!)

		returnType(
			"DefaultingCoalescingConverter<${generic.split(":").first().trim()}>"
		)
	}
	.defaultFirstArgs()
	.requiredFunArg("defaultValue", typeParam)
	.optionalFunArg("required", "Boolean", "false")
	.maybe(extraArguments.isNotEmpty()) {
		extraArguments.forEach {
			rawFunArg(it)
		}
	}
	.maybe(generic != null) {
		defaultLastArgs(generic!!.split(":").first().trim())
	}.maybe(generic == null) {
		defaultLastArgs(typeParam)
	}
	.maybe(extraArguments.isNotEmpty()) {
		extraArguments.forEach {
			converterArg(it.split(":").first().trim().split(" ").last())
		}
	}
	.wrapper("defaulting")
	.wrapperArg("defaultValue")
	.wrapperArg("outputError", "required")
	.wrapperArg("nestedValidator", "validator")
	.build()

internal fun listConverter(
	classDeclaration: KSClassDeclaration,
	name: String,
	typeParam: String,
	extraArguments: ArrayList<String>,
	generic: String?,
): String = ConverterFunctionBuilder(
	"${name.toLowered()}List"
).comment(
	"""
        Creates a $name converter, for lists of arguments.

        @param required Whether command parsing should fail if no arguments could be converted.
        @see ${classDeclaration.simpleName.asString()}
    """.trimIndent()
)
	.converter(classDeclaration.simpleName.asString())
	.returnType("MultiConverter<$typeParam>")
	.maybe(generic != null) {
		rawGeneric(generic!!)

		returnType(
			"MultiConverter<${generic.split(":").first().trim()}>"
		)
	}
	.defaultFirstArgs()
	.optionalFunArg("required", "Boolean", "true")
	.maybe(extraArguments.isNotEmpty()) {
		extraArguments.forEach {
			rawFunArg(it)
		}
	}
	.maybe(generic != null) {
		defaultLastArgs("List<${generic!!.split(":").first().trim()}>")
	}.maybe(generic == null) {
		defaultLastArgs("List<$typeParam>")
	}
	.maybe(extraArguments.isNotEmpty()) {
		extraArguments.forEach {
			converterArg(it.split(":").first().trim().split(" ").last())
		}
	}
	.wrapper("multi")
	.wrapperArg("required")
	.wrapperArg("nestedValidator", "validator")
	.build()

internal fun listChoiceConverter(
	classDeclaration: KSClassDeclaration,
	name: String,
	typeParam: String,
	extraArguments: ArrayList<String>,
	generic: String?,
): String = error("Choice converters are incompatible with list converters.")

internal fun listCoalescingConverter(
	classDeclaration: KSClassDeclaration,
	name: String,
	typeParam: String,
	extraArguments: ArrayList<String>,
	generic: String?,
): String = error("Coalescing converters are incompatible with list converters.")

internal fun singleConverter(
	classDeclaration: KSClassDeclaration,
	name: String,
	typeParam: String,
	extraArguments: ArrayList<String>,
	generic: String?,
): String = ConverterFunctionBuilder(
	name.toLowered()
).comment(
	"""
        Creates a $name converter, for single arguments.

        @see ${classDeclaration.simpleName.asString()}
    """.trimIndent()
)
	.converter(classDeclaration.simpleName.asString())
	.returnType("SingleConverter<$typeParam>")
	.maybe(generic != null) {
		rawGeneric(generic!!)

		returnType(
			"SingleConverter<${generic.split(":").first().trim()}>"
		)
	}
	.defaultFirstArgs()
	.maybe(extraArguments.isNotEmpty()) {
		extraArguments.forEach {
			rawFunArg(it)
		}
	}
	.maybe(generic != null) {
		defaultLastArgs(generic!!.split(":").first().trim())
	}.maybe(generic == null) {
		defaultLastArgs(typeParam)
	}
	.converterArg("validator")
	.maybe(extraArguments.isNotEmpty()) {
		extraArguments.forEach {
			converterArg(it.split(":").first().trim().split(" ").last())
		}
	}
	.build()

internal fun singleChoiceConverter(
	classDeclaration: KSClassDeclaration,
	name: String,
	typeParam: String,
	extraArguments: ArrayList<String>,
	generic: String?,
): String = ConverterFunctionBuilder(
	"${name.toLowered()}Choice"
).comment(
	"""
    Creates a $name choice converter, for a defined set of single arguments.

    @see ${classDeclaration.simpleName.asString()}
    """.trimIndent()
)
	.converter(classDeclaration.simpleName.asString())
	.returnType("SingleConverter<$typeParam>")
	.maybe(generic != null) {
		rawGeneric(generic!!)

		returnType(
			"SingleConverter<${generic.split(":").first().trim()}>"
		)
	}
	.defaultFirstArgs()
	.requiredFunArg("choices", "Map<String, $typeParam>")
	.maybe(extraArguments.isNotEmpty()) {
		extraArguments.forEach {
			rawFunArg(it)
		}
	}
	.maybe(generic != null) {
		defaultLastArgs(generic!!.split(":").first().trim())
	}.maybe(generic == null) {
		defaultLastArgs(typeParam)
	}
	.converterArg("choices")
	.converterArg("validator")
	.maybe(extraArguments.isNotEmpty()) {
		extraArguments.forEach {
			converterArg(it.split(":").first().trim().split(" ").last())
		}
	}
	.build()

internal fun singleCoalescingConverter(
	classDeclaration: KSClassDeclaration,
	name: String,
	typeParam: String,
	extraArguments: ArrayList<String>,
	generic: String?,
): String = ConverterFunctionBuilder(
	"coalesced${name.toCapitalized()}",
).comment(
	"""
       Creates a coalescing $name converter.

       @see ${classDeclaration.simpleName.asString()}
    """.trimIndent()
)
	.converter(classDeclaration.simpleName.asString())
	.returnType("CoalescingConverter<$typeParam>")
	.maybe(generic != null) {
		rawGeneric(generic!!)

		returnType(
			"CoalescingConverter<${generic.split(":").first().trim()}>"
		)
	}
	.defaultFirstArgs()
	.maybe(extraArguments.isNotEmpty()) {
		extraArguments.forEach {
			rawFunArg(it)
		}
	}
	.maybe(generic != null) {
		defaultLastArgs(generic!!.split(":").first().trim())
	}.maybe(generic == null) {
		defaultLastArgs(typeParam)
	}
	.converterArg("validator")
	.maybe(extraArguments.isNotEmpty()) {
		extraArguments.forEach {
			converterArg(it.split(":").first().trim().split(" ").last())
		}
	}
	.build()

internal fun optionalConverter(
	classDeclaration: KSClassDeclaration,
	name: String,
	typeParam: String,
	extraArguments: ArrayList<String>,
	generic: String?,
): String = ConverterFunctionBuilder(
	"optional${name.toCapitalized()}",
).comment(
	"""
        Creates an optional $name converter, for single arguments.

        @param required Whether command parsing should fail if an invalid argument is provided.
        @see ${classDeclaration.simpleName.asString()}
    """.trimIndent()
)
	.converter(classDeclaration.simpleName.asString())
	.returnType("OptionalConverter<$typeParam?>")
	.maybe(generic != null) {
		rawGeneric(generic!!)

		returnType(
			"OptionalConverter<${generic.split(":").first().trim()}?>"
		)
	}
	.defaultFirstArgs()
	.optionalFunArg("required", "Boolean", "false")
	.maybe(extraArguments.isNotEmpty()) {
		extraArguments.forEach {
			rawFunArg(it)
		}
	}
	.maybe(generic != null) {
		defaultLastArgs(generic!!.split(":").first().trim() + "?")
	}
	.maybe(generic == null) {
		defaultLastArgs("$typeParam?")
	}
	.maybe(extraArguments.isNotEmpty()) {
		extraArguments.forEach {
			converterArg(it.split(":").first().trim().split(" ").last())
		}
	}
	.wrapper("optional")
	.wrapperArg("outputError", "required")
	.wrapperArg("nestedValidator", "validator")
	.build()

internal fun optionalChoiceConverter(
	classDeclaration: KSClassDeclaration,
	name: String,
	typeParam: String,
	extraArguments: ArrayList<String>,
	generic: String?,
): String = ConverterFunctionBuilder(
	"optional${name.toCapitalized()}Choice",
).comment(
	"""
        Creates an optional $name choice converter, for a defined set of single arguments.

        @see ${classDeclaration.simpleName.asString()}
    """.trimIndent()
)
	.converter(classDeclaration.simpleName.asString())
	.returnType("OptionalConverter<$typeParam?>")
	.maybe(generic != null) {
		rawGeneric(generic!!)

		returnType(
			"OptionalConverter<${generic.split(":").first().trim()}?>"
		)
	}
	.defaultFirstArgs()
	.requiredFunArg("choices", "Map<String, $typeParam>")
	.optionalFunArg("required", "Boolean", "false")
	.maybe(extraArguments.isNotEmpty()) {
		extraArguments.forEach {
			rawFunArg(it)
		}
	}
	.maybe(generic != null) {
		defaultLastArgs(generic!!.split(":").first().trim() + "?")
	}
	.maybe(generic == null) {
		defaultLastArgs("$typeParam?")
	}
	.converterArg("choices")
	.maybe(extraArguments.isNotEmpty()) {
		extraArguments.forEach {
			converterArg(it.split(":").first().trim().split(" ").last())
		}
	}
	.wrapper("optional")
	.wrapperArg("outputError", "required")
	.wrapperArg("nestedValidator", "validator")
	.build()

internal fun optionalCoalescingConverter(
	classDeclaration: KSClassDeclaration,
	name: String,
	typeParam: String,
	extraArguments: ArrayList<String>,
	generic: String?,
): String = ConverterFunctionBuilder(
	"optionalCoalescing${name.toCapitalized()}",
).comment(
	"""
       Creates an optional coalescing $name converter.

       @see ${classDeclaration.simpleName.asString()}
    """.trimIndent()
)
	.converter(classDeclaration.simpleName.asString())
	.returnType("OptionalCoalescingConverter<$typeParam?>")
	.maybe(generic != null) {
		rawGeneric(generic!!)

		returnType(
			"OptionalCoalescingConverter<${generic.split(":").first().trim()}?>"
		)
	}
	.defaultFirstArgs()
	.optionalFunArg("required", "Boolean", "false")
	.maybe(extraArguments.isNotEmpty()) {
		extraArguments.forEach {
			rawFunArg(it)
		}
	}
	.maybe(generic != null) {
		defaultLastArgs(generic!!.split(":").first().trim() + "?")
	}
	.maybe(generic == null) {
		defaultLastArgs("$typeParam?")
	}
	.maybe(extraArguments.isNotEmpty()) {
		extraArguments.forEach {
			converterArg(it.split(":").first().trim().split(" ").last())
		}
	}
	.wrapper("optional")
	.wrapperArg("outputError", "required")
	.wrapperArg("nestedValidator", "validator")
	.build()
