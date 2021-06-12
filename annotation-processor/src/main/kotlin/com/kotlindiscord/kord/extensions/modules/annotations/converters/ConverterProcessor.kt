@file:Suppress("StringLiteralDuplication", "UnusedPrivateMember")

package com.kotlindiscord.kord.extensions.modules.annotations.converters

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSVisitorVoid
import com.google.devtools.ksp.validate
import com.kotlindiscord.kord.extensions.modules.annotations.*
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

            logger.info("Arguments: \n" + annotation.arguments.joinToString("\n") {
                "    ${it.name?.getShortName()} : ${it.value}"
            })

            val arguments = annotation.arguments
                .associate { it.name?.getShortName() to it.value }
                .filterKeys { it != null }

            val name = arguments["name"]!! as String
            val types = (arguments["types"]!! as ArrayList<KSType>).map { it.declaration.simpleName.asString() }
            val extraImports = arguments["imports"] as ArrayList<String>?
            val extraArguments = arguments["arguments"] as ArrayList<String>? ?: arrayListOf()

            var generic = arguments["generic"] as String?

            if (generic?.isEmpty() == true) {
                generic = null
            }

            val functions: MutableList<String> = mutableListOf()
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

            val typeParam = typeParams.first().type!!.resolve().declaration
            val typeParamName = typeParam.simpleName.asString()
            val isChoice: Boolean = types.contains(ConverterType.CHOICE.name)
            val isCoalescing: Boolean = types.contains(ConverterType.COALESCING.name)

            if (isChoice && isCoalescing) {
                error(
                    "Choice converters are not compatible with coalescing converters. Converter: " +
                        classDeclaration.simpleName.asString()
                )
            }

            types.sorted().forEach {
                logger.info("Current type: $it")

                val func = when (it) {
                    ConverterType.SINGLE.name -> when {
                        isChoice -> singleChoiceConverter(
                            classDeclaration,
                            name,
                            typeParamName,
                            extraArguments,
                            generic,
                        )

                        isCoalescing -> singleCoalescingConverter(
                            classDeclaration,
                            name,
                            typeParamName,
                            extraArguments,
                            generic,
                        )

                        else -> singleConverter(
                            classDeclaration,
                            name,
                            typeParamName,
                            extraArguments,
                            generic,
                        )
                    }

                    ConverterType.OPTIONAL.name -> when {
                        isChoice -> optionalChoiceConverter(
                            classDeclaration,
                            name,
                            typeParamName,
                            extraArguments,
                            generic,
                        )

                        isCoalescing -> optionalCoalescingConverter(
                            classDeclaration,
                            name,
                            typeParamName,
                            extraArguments,
                            generic,
                        )

                        else -> optionalConverter(
                            classDeclaration,
                            name,
                            typeParamName,
                            extraArguments,
                            generic,
                        )
                    }

                    ConverterType.DEFAULTING.name -> when {
                        isChoice -> defaultingChoiceConverter(
                            classDeclaration,
                            name,
                            typeParamName,
                            extraArguments,
                            generic,
                        )

                        isCoalescing -> defaultingCoalescingConverter(
                            classDeclaration,
                            name,
                            typeParamName,
                            extraArguments,
                            generic,
                        )

                        else -> defaultingConverter(
                            classDeclaration,
                            name,
                            typeParamName,
                            extraArguments,
                            generic,
                        )
                    }

                    ConverterType.LIST.name -> when {
                        isChoice -> listChoiceConverter(
                            classDeclaration,
                            name,
                            typeParamName,
                            extraArguments,
                            generic,
                        )

                        isCoalescing -> listCoalescingConverter(
                            classDeclaration,
                            name,
                            typeParamName,
                            extraArguments,
                            generic,
                        )

                        else -> listConverter(
                            classDeclaration,
                            name,
                            typeParamName,
                            extraArguments,
                            generic,
                        )
                    }

                    ConverterType.CHOICE.name -> ""  // Done in the converter functions
                    ConverterType.COALESCING.name -> ""  // Done in the converter functions

                    else -> "// UNSUPPPORTED: $it"
                }.trim('\n')

                functions.add(func)
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
                import com.kotlindiscord.kord.extensions.commands.converters.*
                import com.kotlindiscord.kord.extensions.commands.parser.Arguments
                import dev.kord.common.annotation.KordPreview


            """.trimIndent()

            if (typeParam.simpleName.getShortName() != generic?.split(":")?.first()) {
                outputText += """
                    // Converter type param
                    import ${typeParam.qualifiedName!!.asString()}


                """.trimIndent()
            }

            // Converter type param
            // "import ${typeParam.qualifiedName!!.asString()}"

            if (extraImports != null) {
                outputText += "// Extra imports\n"
                outputText += extraImports.joinToString("\n") { "import $it" }
                outputText += "\n\n"
            }

            outputText += functions.filter { it.isNotEmpty() }.joinToString("\n\n")

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
}

internal fun String.toCapitalized() =
    replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }

internal fun String.toLowered() =
    replaceFirstChar { if (it.isUpperCase()) it.lowercase(Locale.getDefault()) else it.toString() }
