@file:Suppress("StringLiteralDuplication", "UNUSED_PARAMETER")

// UNUSED_PARAMETER is suppressed in order to make code more abstract :pineapple:

package com.kotlindiscord.kord.extensions.modules.extra.mappings.utils

import com.kotlindiscord.kord.extensions.modules.extra.mappings.utils.linkie.mapIfNotNullOrNotEquals
import com.kotlindiscord.kord.extensions.modules.extra.mappings.utils.linkie.stringPairs
import me.shedaniel.linkie.*
import me.shedaniel.linkie.namespaces.MojangHashedNamespace
import me.shedaniel.linkie.utils.*

private const val PAGE_SIZE = 3

private typealias ClassResults = QueryResult<MappingsContainer, ClassResultList>
private typealias Matches<T> = Map<T, T>

/** Given a set of result classes, format them into a list of pages for the paginator. **/
fun classesToPages(
    namespace: Namespace,
    classes: List<Class>
): List<Pair<String, String>> {
    val pages = mutableListOf<Pair<String, String>>()

    classes.chunked(PAGE_SIZE).forEach { result ->
        val shortPage = result.joinToString("\n\n") { clazz ->
            var text = ""

            text += "**Class:** `${clazz.optimumName}`\n"

            val (clientName, serverName) = clazz.obfName.stringPairs()

            if (clientName != null && clientName.isNotEmpty()) {
                if (serverName == null) {
                    text += "**Name:** `$clientName` -> "
                } else {
                    text += "**Client:** `$clientName` -> "
                }

                text += "`${clazz.intermediaryName}`" +
                    (clazz.mappedName.mapIfNotNullOrNotEquals(clazz.intermediaryName) { name ->
                        " -> `$name`"
                    } ?: "")
            }

            if (serverName != null && serverName.isNotEmpty()) {
                if (clientName != null) {
                    text += "\n"
                }

                text += "**Server:** `$serverName` -> "

                text += "`${clazz.intermediaryName}`" +
                    (clazz.mappedName.mapIfNotNullOrNotEquals(clazz.intermediaryName) { name ->
                        " -> `$name`"
                    } ?: "")
            }

            text.trimEnd('\n')
        }

        val longPage = result.joinToString("\n\n") { clazz ->
            var text = ""

            text += "**Class:** `${clazz.optimumName}`\n"

            val (clientName, serverName) = clazz.obfName.stringPairs()

            if (clientName != null && clientName.isNotEmpty()) {
                if (serverName == null) {
                    text += "**Name:** `$clientName` -> "
                } else {
                    text += "**Client:** `$clientName` -> "
                }

                text += "`${clazz.intermediaryName}`" +
                    (clazz.mappedName.mapIfNotNullOrNotEquals(clazz.intermediaryName) { name ->
                        " -> `$name`"
                    } ?: "")
            }

            if (serverName != null && serverName.isNotEmpty()) {
                if (clientName != null) {
                    text += "\n"
                }

                text += "**Server:** `$serverName` -> "

                text += "`${clazz.intermediaryName}`" +
                    (clazz.mappedName.mapIfNotNullOrNotEquals(clazz.intermediaryName) { name ->
                        " -> `$name`"
                    } ?: "")
            }

            text += "\n"

            if (namespace.supportsAT()) {
                text += "**Access Transformer:** `public " +
                    clazz.intermediaryName.replace('/', '.') +
                    "`"
            } else if (namespace.supportsAW()) {
                text += "\n" +
                    "**Access Widener:** `accessible class ${clazz.optimumName}`"
            }

            text.trimEnd('\n')
        }

        pages.add(Pair(shortPage, longPage))
    }

    return pages
}

/** Given a set of result classes, format them into a list of pages for the paginator. **/
fun classesToPages(
    namespace: Namespace,
    queryResult: QueryResult<MappingsContainer, ClassResultList>
) =
    classesToPages(namespace, queryResult.map { it.map { inner -> inner.value }.toList() }.value)

/**
 * A convenience function variable for having an unused MappingsContainer argument.
 * This is to allow a more general function to be used for all queries.
 */
val classesToPages = { namespace: Namespace, _: MappingsContainer, classes: ClassResults ->
    classesToPages(namespace, classes)
}

/** Given a set of result fields, format them into a list of pages for the paginator. **/
fun fieldsToPages(
    namespace: Namespace,
    mappings: MappingsContainer,
    fields: List<Pair<Class, Field>>
): List<Pair<String, String>> {
    val pages = mutableListOf<Pair<String, String>>()

    fields.chunked(PAGE_SIZE).forEach { result ->
        val shortPage = result.joinToString("\n\n") {
            val (clazz, field) = it
            val mappedDesc = field.getMappedDesc(mappings)

            var text = ""

            text += "**Field:** `${clazz.optimumName}::${field.optimumName}`\n"

            val (clientName, serverName) = field.obfName.stringPairs()

            if (!clientName.isNullOrEmpty()) {
                if (!serverName.isNullOrEmpty()) {
                    text += "**Name:** `$clientName` -> "
                } else {
                    text += "**Client:** `$clientName` -> "
                }

                text += "`${field.intermediaryName}`" +
                    (field.mappedName.mapIfNotNullOrNotEquals(field.intermediaryName) { name ->
                        " -> `$name`"
                    } ?: "")
            }

            if (!serverName.isNullOrEmpty()) {
                if (!clientName.isNullOrEmpty()) {
                    text += "\n"
                }

                text += "**Server:** `$serverName` -> "

                text += "`${field.intermediaryName}`" +
                    (field.mappedName.mapIfNotNullOrNotEquals(field.intermediaryName) { name ->
                        " -> `$name`"
                    } ?: "")
            }

            if (namespace.supportsFieldDescription()) {
                text += "\n"
                text += "**Types:** `${mappedDesc.localiseFieldDesc()}`"
            }

            text
        }

        val longPage = result.joinToString("\n\n") {
            val (clazz, field) = it
            val mappedDesc = field.getMappedDesc(mappings)

            var text = ""

            text += "**Field:** `${clazz.optimumName}::${field.optimumName}`\n"

            val (clientName, serverName) = field.obfName.stringPairs()

            if (!clientName.isNullOrEmpty()) {
                if (!serverName.isNullOrEmpty()) {
                    text += "**Name:** `$clientName` -> "
                } else {
                    text += "**Client:** `$clientName` -> "
                }

                text += "`${field.intermediaryName}`" +
                    (field.mappedName.mapIfNotNullOrNotEquals(field.intermediaryName) { name ->
                        " -> `$name`"
                    } ?: "")
            }

            if (!serverName.isNullOrEmpty()) {
                if (!clientName.isNullOrEmpty()) {
                    text += "\n"
                }

                text += "**Server:** `$serverName` -> "

                text += "`${field.intermediaryName}`" +
                    (field.mappedName.mapIfNotNullOrNotEquals(field.intermediaryName) { name ->
                        " -> `$name`"
                    } ?: "")
            }

            if (namespace.supportsFieldDescription()) {
                text += "\n"
                text += "**Types:** `${mappedDesc.localiseFieldDesc()}`"
            }

            text += "\n"

            if (namespace.supportsMixin()) {
                text += "\n"

                text += "**Mixin Target:** `" +
                    "L${clazz.optimumName};" +
                    field.optimumName +
                    ":" +
                    mappedDesc +
                    "`"
            }

            if (namespace.supportsAT()) {
                text += "\n"

                text += "**Access Transformer:** `${field.intermediaryName} # ${field.optimumName}`"
            } else if (namespace.supportsAW()) {
                text += "\n"

                text += "**Access Widener:** `accessible field ${clazz.optimumName} ${field.optimumName} $mappedDesc`"
            }

            text.trimEnd('\n')
        }

        pages.add(Pair(shortPage, longPage))
    }

    return pages
}

/** Given a set of result fields, format them into a list of pages for the paginator. **/
fun fieldsToPages(
    namespace: Namespace,
    mappings: MappingsContainer,
    queryResult: QueryResult<MappingsContainer, FieldResultList>
) =
    fieldsToPages(namespace, mappings, queryResult.map { it.map { inner -> inner.value }.toList() }.value)

/** Given a set of result methods, format them into a list of pages for the paginator. **/
fun methodsToPages(
    namespace: Namespace,
    mappings: MappingsContainer,
    methods: List<Pair<Class, Method>>
): List<Pair<String, String>> {
    val pages = mutableListOf<Pair<String, String>>()

    methods.chunked(PAGE_SIZE).forEach { result ->
        val shortPage = result.joinToString("\n\n") {
            val (clazz, method) = it
            var text = ""

            text += "**Method:** `${clazz.optimumName}::${method.optimumName}`\n"

            val (clientName, serverName) = method.obfName.stringPairs()

            if (!clientName.isNullOrEmpty()) {
                if (!serverName.isNullOrEmpty()) {
                    text += "**Name:** `$clientName` -> "
                } else {
                    text += "**Client:** `$clientName` -> "
                }

                text += "`${method.intermediaryName}`" +
                    (method.mappedName.mapIfNotNullOrNotEquals(method.intermediaryName) { name ->
                        " -> `$name`"
                    } ?: "")
            }

            if (!serverName.isNullOrEmpty()) {
                if (!clientName.isNullOrEmpty()) {
                    text += "\n"
                }

                text += "**Server:** `$serverName` -> "

                text += "`${method.intermediaryName}`" +
                    (method.mappedName.mapIfNotNullOrNotEquals(method.intermediaryName) { name ->
                        " -> `$name`"
                    } ?: "")
            }

            text.trimEnd('\n')
        }

        val longPage = result.joinToString("\n\n") {
            val (clazz, method) = it
            val mappedDesc = method.getMappedDesc(mappings)

            var text = ""

            text += "**Method:** `${clazz.optimumName}::${method.optimumName}`\n"

            val (clientName, serverName) = method.obfName.stringPairs()

            if (clientName != null && clientName.isNotEmpty()) {
                if (serverName == null) {
                    text += "**Name:** `$clientName` -> "
                } else {
                    text += "**Client:** `$clientName` -> "
                }

                text += "`${method.intermediaryName}`" +
                    (method.mappedName.mapIfNotNullOrNotEquals(method.intermediaryName) { name ->
                        " -> `$name`"
                    } ?: "")
            }

            if (serverName != null && serverName.isNotEmpty()) {
                if (clientName != null) {
                    text += "\n"
                }

                text += "**Server:** `$serverName` -> "

                text += "`${method.intermediaryName}`" +
                    (method.mappedName.mapIfNotNullOrNotEquals(method.intermediaryName) { name ->
                        " -> `$name`"
                    } ?: "")
            }

            text += "\n"

            if (namespace.supportsMixin()) {
                text += "\n"

                text += "**Mixin Target** `" +
                    "L${clazz.optimumName};" +
                    method.optimumName +
                    mappedDesc +
                    "`"
            }

            if (namespace.supportsAT()) {
                text += "\n"

                text += "**Access Transformer** `public" + clazz.optimumName.replace('/', '.') +
                    method.intermediaryName +
                    mappedDesc +
                    " # ${method.optimumName}`"
            } else if (namespace.supportsAW()) {
                text += "\n"

                text += "**Access Widener** `accessible method ${clazz.optimumName} ${method.optimumName} $mappedDesc`"
            }

            text.trimEnd('\n')
        }

        pages.add(Pair(shortPage, longPage))
    }

    return pages
}

/** Given a set of result methods, format them into a list of pages for the paginator. **/
fun methodsToPages(
    namespace: Namespace,
    mappings: MappingsContainer,
    queryResult: QueryResult<MappingsContainer, MethodResultList>
) = methodsToPages(namespace, mappings, queryResult.map { it.map { inner -> inner.value }.toList() }.value)

/** Given a set of class mapping matches, format them into a list of pages for the paginator. **/
fun classMatchesToPages(
    matches: List<Pair<Class, Class>>
): List<String> {
    val pages = mutableListOf<String>()

    for (match in matches.chunked(PAGE_SIZE)) {
        val text = match.joinToString("\n\n") { (input, output) ->

            val inputName = input.mappedName ?: input.optimumName
            val outputName = output.mappedName ?: output.optimumName

            "**Class:** `$inputName` -> `$outputName`"
        }
        pages.add(text)
    }

    return pages
}

/** Convienence function for making code more generalized. */
val classMatchesToPages = { _: MappingsContainer, classMatches: Matches<Class> ->
    classMatchesToPages(classMatches.toList())
}

/** Given a set of field mapping matches, format them into a list of pages for the paginator. **/
fun fieldMatchesToPages(
    outputContainer: MappingsContainer,
    matches: List<Pair<Pair<Class, Field>, Pair<Class, Field>>>
): List<String> {
    val pages = mutableListOf<String>()

    for (match in matches.chunked(PAGE_SIZE)) {
        val page = match.joinToString("\n\n") {
            val (inputClass, inputField) = it.first
            val (outputClass, outputField) = it.second
            val mappedDesc = outputField.getMappedDesc(outputContainer)

            val inputName = inputField.mappedName ?: inputField.optimumName
            val outputName = outputField.mappedName ?: outputField.optimumName
            val inputClassName = inputClass.mappedName ?: inputClass.optimumName
            val outputClassName = outputClass.mappedName ?: outputClass.optimumName

            var text = "**Field:** `$inputClassName::$inputName` -> `$outputClassName::$outputName`"

            val namespace = if (outputContainer.namespace == "hashed-mojmap") {
                // thanks linkie you're ruining everything
                MojangHashedNamespace
            } else {
                Namespaces[outputContainer.namespace]
            }

            if (namespace.supportsFieldDescription()) {
                text += "\n"
                text += "**Types:** `${mappedDesc.localiseFieldDesc()}`"
            }

            text
        }
        pages.add(page)
    }

    return pages
}

/** Convienence function for making code more generalized. */
val fieldMatchesToPages = { outputContainer: MappingsContainer, fieldMatches: Matches<Pair<Class, Field>> ->
    fieldMatchesToPages(outputContainer, fieldMatches.toList())
}

/** Given a set of method mapping matches, format them into a list of pages for the paginator. **/
fun methodMatchesToPages(
    outputContainer: MappingsContainer,
    matches: List<Pair<Pair<Class, Method>, Pair<Class, Method>>>
): List<String> {
    val pages = mutableListOf<String>()

    for (match in matches.chunked(PAGE_SIZE)) {
        val page = match.joinToString("\n\n") {
            val (inputClass, inputMethod) = it.first
            val (outputClass, outputMethod) = it.second
            val mappedDesc = outputMethod.getMappedDesc(outputContainer)

            val inputName = inputMethod.mappedName ?: inputMethod.optimumName
            val outputName = outputMethod.mappedName ?: outputMethod.optimumName
            val inputClassName = inputClass.mappedName ?: inputClass.optimumName
            val outputClassName = outputClass.mappedName ?: outputClass.optimumName

            var text = "**Method:** `$inputClassName::$inputName` -> `$outputClassName::$outputName`"

            text += "\n" +
                "**Description:** `$mappedDesc`"

            text
        }
        pages.add(page)
    }

    return pages
}

/** Convienence function for making code more generalized. */
val methodMatchesToPages = { outputContainer: MappingsContainer, methodMatches: Matches<Pair<Class, Method>> ->
    methodMatchesToPages(outputContainer, methodMatches.toList())
}

/** Attempt to get an obfuscated name from three possible states. */
val Obf.preferredName: String?
    get() = merged ?: client ?: server
