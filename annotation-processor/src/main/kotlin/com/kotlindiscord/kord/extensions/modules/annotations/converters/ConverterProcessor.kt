package com.kotlindiscord.kord.extensions.modules.annotations.converters

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSVisitorVoid
import com.google.devtools.ksp.validate
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

            val arguments = annotation.arguments.associate { it.name?.getShortName() to it.value }
                .filterKeys { it != null }

            val name = arguments["name"]!! as String
            val types = (arguments["types"]!! as ArrayList<KSType>).map { it.declaration.simpleName.asString() }
            val extraImports = arguments["imports"] as ArrayList<String>?

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
            val hasChoice: Boolean = types.contains(ConverterType.CHOICE.name)

            types.sorted().forEach {
                logger.info("Current type: $it")

                val func = when (it) {
                    ConverterType.SINGLE.name -> createSingleConverterFunction(
                        classDeclaration,
                        name,
                        typeParamName,
                        hasChoice
                    )

                    ConverterType.OPTIONAL.name -> createOptionalConverterFunction(
                        classDeclaration,
                        name,
                        typeParamName,
                        hasChoice
                    )

                    ConverterType.DEFAULTING.name -> createDefaultingConverterFunction(
                        classDeclaration,
                        name,
                        typeParamName,
                        hasChoice
                    )

                    ConverterType.LIST.name -> createListConverterFunction(
                        classDeclaration,
                        name,
                        typeParamName,
                        hasChoice
                    )

                    ConverterType.CHOICE.name -> ""  // Done in the converter functions

                    else -> "// UNSUPPPORTED: $it"
                }

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
                
                // Converter type param
                import ${typeParam.qualifiedName!!.asString()}
                
                // Original converter class, for safety
                import ${classDeclaration.qualifiedName!!.asString()}
                
                // Imports that all converters need
                import com.kotlindiscord.kord.extensions.commands.converters.*
                import com.kotlindiscord.kord.extensions.commands.parser.Arguments
                import dev.kord.common.annotation.KordPreview
                
                
            """.trimIndent()

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

        private fun createDefaultingConverterFunction(
            classDeclaration: KSClassDeclaration,
            name: String,
            typeParam: String,
            hasChoice: Boolean
        ): String = if (!hasChoice) {
            """
                /**
                 * Creates a defaulting $name converter, for single arguments.
                 * 
                 * @param defaultValue Default value to use if no argument was provided.
                 * @see ${classDeclaration.simpleName.asString()}
                 */
                public fun Arguments.defaulting${name.toCapitalized()}(
                    displayName: String,
                    description: String,
                    defaultValue: $typeParam,
                    validator: Validator<$typeParam> = null,
                ): DefaultingConverter<$typeParam> =
                    arg(
                        displayName = displayName,
                        description = description,

                        converter = ${classDeclaration.simpleName.asString()}()
                            .toDefaulting(defaultValue, nestedValidator = validator)
                    )
            """.trimIndent()
        } else {
            """
                /**
                 * Creates a defaulting $name choice converter, for a defined set of single arguments.
                 * 
                 * @param defaultValue Default value to use if no argument was provided.
                 * @see ${classDeclaration.simpleName.asString()}
                 */
                public fun Arguments.defaulting${name.toCapitalized()}Choice(
                    displayName: String,
                    description: String,
                    defaultValue: $typeParam,
                    choices: Map<String, $typeParam>,
                    validator: Validator<$typeParam> = null,
                ): DefaultingConverter<$typeParam> =
                    arg(
                        displayName = displayName,
                        description = description,

                        converter = ${classDeclaration.simpleName.asString()}(choices)
                            .toDefaulting(defaultValue, nestedValidator = validator)
                    )
                """.trimIndent()
        }

        private fun createListConverterFunction(
            classDeclaration: KSClassDeclaration,
            name: String,
            typeParam: String,
            hasChoice: Boolean
        ): String =
            if (!hasChoice) {
                """
                    /**
                     * Creates a $name converter, for lists of arguments.
                     * 
                     * @param required Whether command parsing should fail if no arguments could be converted.
                     * @see ${classDeclaration.simpleName.asString()}
                     */
                    public fun Arguments.${name.toLowered()}List(
                        displayName: String,
                        description: String,
                        required: Boolean = true,
                        validator: Validator<List<$typeParam>> = null,
                    ): MultiConverter<$typeParam> =
                        arg(
                            displayName = displayName,
                            description = description,
    
                            converter = ${classDeclaration.simpleName.asString()}()
                                .toMulti(required, nestedValidator = validator)
                        )
                """.trimIndent()
            } else {
                // There are no list-based choice converters
                error("Choice converters are incompatible with list converters.")
            }

        private fun createSingleConverterFunction(
            classDeclaration: KSClassDeclaration,
            name: String,
            typeParam: String,
            hasChoice: Boolean
        ): String =
            if (!hasChoice) {
                """
                    /**
                     * Creates a $name converter, for single arguments.
                     * 
                     * @see ${classDeclaration.simpleName.asString()}
                     */
                    public fun Arguments.${name.toLowered()}(
                        displayName: String,
                        description: String,
                        validator: Validator<$typeParam> = null,
                    ): SingleConverter<$typeParam> =
                        arg(
                            displayName = displayName,
                            description = description,
    
                            converter = ${classDeclaration.simpleName.asString()}(validator)
                        )
                """.trimIndent()
            } else {
                """
                    /**
                     * Creates a $name choice converter, for a defined set of single arguments.
                     * 
                     * @see ${classDeclaration.simpleName.asString()}
                     */
                    public fun Arguments.${name.toLowered()}Choice(
                        displayName: String,
                        description: String,
                        choices: Map<String, $typeParam>,
                        validator: Validator<$typeParam> = null,
                    ): SingleConverter<$typeParam> =
                        arg(
                            displayName = displayName,
                            description = description,
    
                            converter = ${classDeclaration.simpleName.asString()}(choices, validator)
                        )
                """.trimIndent()
            }

        private fun createOptionalConverterFunction(
            classDeclaration: KSClassDeclaration,
            name: String,
            typeParam: String,
            hasChoice: Boolean
        ): String =
            if (!hasChoice) {
                """
                    /**
                     * Creates an optional $name converter, for single arguments.
                     * 
                     * @param required Whether command parsing should fail if an invalid argument is provided.
                     * @see ${classDeclaration.simpleName.asString()}
                     */
                    public fun Arguments.optional${name.toCapitalized()}(
                        displayName: String,
                        description: String,
                        required: Boolean = false,
                        validator: Validator<$typeParam?> = null,
                    ): OptionalConverter<$typeParam?> =
                        arg(
                            displayName = displayName,
                            description = description,
    
                            converter = ${classDeclaration.simpleName.asString()}()
                                .toOptional(outputError = required, nestedValidator = validator)
                        )
                """.trimIndent()
            } else {
                """
                    /**
                     * Creates an optional $name choice converter, for a defined set of single arguments.
                     * 
                     * @see ${classDeclaration.simpleName.asString()}
                     */
                    public fun Arguments.optional${name.toCapitalized()}Choice(
                        displayName: String,
                        description: String,
                        choices: Map<String, $typeParam>,
                        validator: Validator<$typeParam?> = null,
                    ): OptionalConverter<$typeParam?> =
                        arg(
                            displayName = displayName,
                            description = description,
    
                            converter = ${classDeclaration.simpleName.asString()}(choices)
                                .toOptional(nestedValidator = validator)
                        )
                """.trimIndent()
            }

        private fun String.toCapitalized() =
            replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }

        private fun String.toLowered() =
            replaceFirstChar { if (it.isUpperCase()) it.lowercase(Locale.getDefault()) else it.toString() }
    }
}
