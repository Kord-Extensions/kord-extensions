/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

@file:Suppress("UncheckedCast")

package dev.kordex.core.annotations.converters

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import dev.kordex.core.annotations.orNull
import kotlin.collections.component1
import kotlin.collections.component2

/**
 * Class representing the arguments that are defined within a converter annotation, extracted from its declaration.
 *
 * @property annotation Annotation definition to extract data from.
 */
@Suppress("UNCHECKED_CAST")
public data class ConverterAnnotationArgs(
	public val annotation: KSAnnotation,
	private val logger: KSPLogger,
) {
	init {
		logger.info("Building arguments class for annotation: $annotation")
		logger.info("Arguments: ${annotation.arguments.size}")

		annotation.arguments.forEach { arg ->
			logger.info(" -> ${arg.name?.getShortName()} -> ${arg.value}")
		}
	}

	/** @suppress **/
	private val argMap: Map<String?, Any?> =
		annotation.arguments
			.associate { it.name?.getShortName() to it.value }
			.filterKeys { it != null }

	/** @suppress **/
	public val names: ArrayList<String> =
		argMap["names"] as ArrayList<String>? ?: arrayListOf()

	/** @suppress **/
	public val types: List<ConverterType> =
		(argMap["types"]!! as ArrayList<Any>).mapNotNull {
			logger.info("ConverterType: $it")

			if (it is KSClassDeclaration) {
				ConverterType.fromName(it.simpleName.asString())
			} else if (it is KSType) {
				ConverterType.fromName(it.declaration.simpleName.asString())
			} else {
				null
			}
		}

	/** @suppress **/
	public val imports: ArrayList<String> =
		argMap["imports"] as ArrayList<String>? ?: arrayListOf()

	/** @suppress **/
	public val builderConstructorArguments: ArrayList<String> =
		argMap["builderConstructorArguments"] as ArrayList<String>? ?: arrayListOf()

	/** @suppress **/
	public val builderGeneric: String? =
		(argMap["builderGeneric"] as String).orNull()

	/** @suppress **/
	public val builderFields: ArrayList<String> =
		argMap["builderFields"] as ArrayList<String>? ?: arrayListOf()

	/** @suppress **/
	public val builderBuildFunctionStatements: ArrayList<String> =
		argMap["builderBuildFunctionStatements"] as ArrayList<String>? ?: arrayListOf()

	/** @suppress **/
	public val builderBuildFunctionPreStatements: ArrayList<String> =
		argMap["builderBuildFunctionPreStatements"] as ArrayList<String>? ?: arrayListOf()

	/** @suppress **/
	public val builderExtraStatements: ArrayList<String> =
		argMap["builderExtraStatements"] as ArrayList<String>? ?: arrayListOf()

	/** @suppress **/
	public val builderInitStatements: ArrayList<String> =
		argMap["builderInitStatements"] as ArrayList<String>? ?: arrayListOf()

	/** @suppress **/
	public val builderSuffixedWhere: String? =
		(argMap["builderSuffixedWhere"] as String).orNull()

	/** @suppress **/
	public val functionGeneric: String? =
		(argMap["functionGeneric"] as String).orNull()

	/** @suppress **/
	public val functionBuilderArguments: ArrayList<String> =
		argMap["functionBuilderArguments"] as ArrayList<String>? ?: arrayListOf()

	/** @suppress **/
	public val functionSuffixedWhere: String? =
		(argMap["functionSuffixedWhere"] as String).orNull()
}
