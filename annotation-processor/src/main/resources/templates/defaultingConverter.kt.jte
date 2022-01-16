@import java.util.ArrayList
@import java.util.List
@import com.google.devtools.ksp.symbol.KSClassDeclaration
@import com.kotlindiscord.kord.extensions.modules.annotations.converters.ConverterProcessor

@param KSClassDeclaration classDeclaration
@param String name
@param String typeParam
@param ArrayList<String> extraArguments
@param String generic

@param String packageName
@param String qualifiedClassName
@param String qualifiedTypeName

@param String capitalisedConverterName

@param List<String> imports

@tag.base(packageName, qualifiedClassName, qualifiedTypeName, imports)


/**
 * Creates a defaulting ${name} converter, for single arguments.
 *
 * \@param defaultValue Default value to use if no argument was provided.
 * \@see ${classDeclaration.getSimpleName().asString()}
 */
public fun Arguments.defaulting${ConverterProcessor.Companion.capitalize(name)}(
    displayName: String,
    description: String,
    defaultValue: Boolean,
    required: Boolean = false,
    validator: Validator<Boolean> = null,
): DefaultingConverter<${typeParam}> =
    arg(
        displayName = displayName,
        description = description,

        converter = BooleanConverter()
            .toDefaulting(
                defaultValue = defaultValue,
                outputError = required,
                nestedValidator = validator,
            )
    )
