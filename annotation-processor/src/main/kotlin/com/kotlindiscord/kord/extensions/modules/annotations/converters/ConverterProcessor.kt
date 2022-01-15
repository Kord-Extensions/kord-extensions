/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

@file:Suppress("StringLiteralDuplication", "UnusedPrivateMember")

package com.kotlindiscord.kord.extensions.modules.annotations.converters

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSVisitorVoid
import com.google.devtools.ksp.validate
import com.kotlindiscord.kord.extensions.modules.annotations.converters.builders.builderClass
import com.kotlindiscord.kord.extensions.modules.annotations.converters.builders.builderFunction
import java.util.*

/**
 * Annotation processor for KordEx converters.
 */
public class ConverterProcessor(
    private val generator: CodeGenerator,
    private val logger: KSPLogger
) : SymbolProcessor {
    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver.getSymbolsWithAnnotation(
            "com.kotlindiscord.kord.extensions.modules.annotations.converters.Converter"
        )

        val ret = symbols.filter { !it.validate() }.toList()

        ret.forEach {
            logger.warn("Unable to validate: $it")
        }

        symbols.filter { it is KSClassDeclaration && it.validate() }
            .forEach { it.accept(ConverterVisitor(), Unit) }

        return ret
    }

    /** Converter annotation visitor. **/
    public inner class ConverterVisitor : KSVisitorVoid() {
        override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
            val annotation = classDeclaration.annotations.filter {
                it.annotationType.resolve().declaration.qualifiedName?.asString() ==
                    "com.kotlindiscord.kord.extensions.modules.annotations.converters.Converter"
            }.first()

            logger.info("Found annotation", annotation)

            val arguments = ConverterAnnotationArgs(annotation)

            logger.info("Arguments: \n" + annotation.arguments.joinToString("\n") {
                "    ${it.name?.getShortName()} : ${it.value}"
            })

            val superTypes = classDeclaration.superTypes.map { it.resolve() }.toList()

            logger.info(
                "Super types (${superTypes.size}): " + superTypes.joinToString(", ") {
                    it.declaration.simpleName.asString()
                }
            )

            val superType = superTypes.first()
            val typeParams = superType.arguments

            logger.info(
                "Types params (${typeParams.size}): " + typeParams.joinToString(", ") {
                    it.type!!.resolve().declaration.simpleName.asString()
                }
            )

            val typeVars = typeParams.map { it.type!!.resolve().declaration }
            val typeVarName = typeVars.first().simpleName.asString()

            val strings: MutableList<String> = mutableListOf()

            for (name in arguments.names) {
                if (arguments.types.count { type -> type.order == 1 } != 1) {
                    error(
                        "Types list must contain exactly one of COALESCING or SINGLE. Convreter: " +
                            classDeclaration.simpleName.asString()
                    )
                }

                val primaryType = arguments.types.first { type -> type.order == 1 }  // Order 1 is single/coalescing
                val isChoice = ConverterType.CHOICE in arguments.types

                val baseTypes = arguments.types
                    .filter { it.order == 0 }

                strings.add(
                    generate(
                        classDeclaration,
                        arguments,
                        name,
                        typeVarName,

                        if (isChoice) {
                            listOf(primaryType, ConverterType.CHOICE)
                        } else {
                            listOf(primaryType)
                        }
                    )
                )

                baseTypes.forEach { type ->
                    strings.add(
                        generate(
                            classDeclaration,
                            arguments,
                            name,
                            typeVarName,

                            if (isChoice) {
                                listOf(primaryType, type, ConverterType.CHOICE)
                            } else {
                                listOf(primaryType, type)
                            }
                        )
                    )
                }
            }

            var outputText = """
                @file:OptIn(
                    KordPreview::class,
                    ConverterToDefaulting::class,
                    ConverterToMulti::class,
                    ConverterToOptional::class
                )

                package ${classDeclaration.packageName.asString()}
                
                // Original converter class, for safety
                import ${classDeclaration.qualifiedName!!.asString()}
                
                // Imports that all converters need
                import com.kotlindiscord.kord.extensions.InvalidArgumentException
                import com.kotlindiscord.kord.extensions.commands.Arguments
                import com.kotlindiscord.kord.extensions.commands.converters.*
                import com.kotlindiscord.kord.extensions.commands.converters.builders.*
                import dev.kord.common.annotation.KordPreview

                // Converter type params
                
            """.trimIndent()

            val ignoredGenerics = listOfNotNull(
                arguments.builderGeneric?.split(":")?.first(),
                arguments.functionGeneric?.split(":")?.first()
            )

            typeVars.forEach {
                if (it.simpleName.getShortName() !in ignoredGenerics) {
                    outputText += "import ${it.qualifiedName!!.asString()}\n"
                }
            }

            outputText += "\n\n"

            if (arguments.imports.isNotEmpty()) {
                outputText += "// Extra imports\n"
                outputText += arguments.imports.joinToString("\n") { "import $it" }
                outputText += "\n\n"
            }

            outputText += strings.filter { it.isNotEmpty() }.joinToString("\n\n")

            val file = generator.createNewFile(
                Dependencies(true, classDeclaration.containingFile!!),
                classDeclaration.packageName.asString(),
                classDeclaration.simpleName.asString() + "Functions"
            )

            outputText += "\n"

            file.write(outputText.encodeToByteArray())
            file.flush()
            file.close()
        }
    }

    internal fun generate(
        classDeclaration: KSClassDeclaration,
        arguments: ConverterAnnotationArgs,
        converterName: String,
        argumentTypeString: String,
        types: List<ConverterType>
    ): String {
        val classBuilder = builderClass {
            comment = classComment(converterName, classDeclaration.simpleName.asString())

            name = converterName.toCapitalized()
            converterClass = classDeclaration.simpleName.asString()
            argumentType = argumentTypeString

            builderGeneric = arguments.builderGeneric

            arguments.builderConstructorArguments.forEach {
                builderArg(it)
            }

            arguments.builderFields.forEach {
                builderField(it)
            }

            types(types)
        }

        val function = builderFunction {
            comment = functionComment(
                converterName,
                classBuilder.converterType.splitUpper().joinToString(" ") { it.toLowered() },
                classBuilder.builderType
            )

            name = classBuilder.getFunctionName(converterName)

            builderGeneric = arguments.builderGeneric
            functionGeneric = arguments.functionGeneric

            argumentType = argumentTypeString
            builderType = classBuilder.builderType
            converterType = classBuilder.converterType

            arguments.functionBuilderArguments.forEach {
                builderArg(it)
            }
        }

        return """
            ${classBuilder.result!!.trim()}
            
            ${function.trim()}
        """.trimIndent().trim('\n', ' ')
    }

    internal fun classComment(name: String, see: String): String = """
        Builder class for $name converters. Used to construct a converter based on the given options.
        
        @see $see
    """.trimIndent()

    internal fun functionComment(name: String, type: String, see: String): String = """
        Creates a $name $type converter.
        
        @see $see
    """.trimIndent()
}

internal fun String.toCapitalized() =
    replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }

internal fun String.toLowered() =
    replaceFirstChar { if (it.isUpperCase()) it.lowercase(Locale.getDefault()) else it.toString() }

internal fun String.splitUpper(): List<String> {
    val parts: MutableList<String> = mutableListOf()
    var currentPart = ""

    for (char in this) {
        if (currentPart.isEmpty()) {
            currentPart += char
            continue
        }

        if (char.isUpperCase()) {
            parts.add(currentPart)
            currentPart = "" + char
            continue
        }

        currentPart += char
    }

    if (currentPart.isNotEmpty()) {
        parts.add(currentPart)
    }

    return parts.toList()
}
