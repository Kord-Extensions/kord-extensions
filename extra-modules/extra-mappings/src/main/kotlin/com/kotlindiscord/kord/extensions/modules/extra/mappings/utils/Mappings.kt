@file:Suppress("StringLiteralDuplication")

package com.kotlindiscord.kord.extensions.modules.extra.mappings.utils

import com.kotlindiscord.kord.extensions.modules.extra.mappings.utils.linkie.mapIfNotNullOrNotEquals
import com.kotlindiscord.kord.extensions.modules.extra.mappings.utils.linkie.stringPairs
import me.shedaniel.linkie.*
import me.shedaniel.linkie.utils.*

private const val PAGE_SIZE = 3

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

fun classesToPages(
    namespace: Namespace,
    queryResult: QueryResult<MappingsContainer, ClassResultList>
) =
    classesToPages(namespace, queryResult.map { it.map { inner -> inner.value }.toList() }.value)

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

fun methodsToPages(
    namespace: Namespace,
    mappings: MappingsContainer,
    queryResult: QueryResult<MappingsContainer, MethodResultList>
) = methodsToPages(namespace, mappings, queryResult.map { it.map { inner -> inner.value }.toList() }.value)

fun classMatchesToPages(
    matches: List<Pair<Class, Class>>
): List<String> {
    val pages = mutableListOf<String>()

    for (match in matches) {
        val (input, output) = match

        val inputName = input.mappedName ?: input.optimumName
        val outputName = output.mappedName ?: output.optimumName

        val text = "**Class:** `$inputName` -> `$outputName`"

        pages.add(text)
    }

    return pages
}

fun fieldMatchesToPages(
    outputContainer: MappingsContainer,
    matches: List<Pair<Field, Field>>
): List<String> {
    val pages = mutableListOf<String>()

    for (match in matches.chunked(PAGE_SIZE)) {
        val page = match.joinToString("\n\n") {
            val (input, output) = it
            val mappedDesc = output.getMappedDesc(outputContainer)

            val inputName = input.mappedName ?: input.optimumName
            val outputName = output.mappedName ?: output.optimumName

            var text = "**Field:** `$inputName` -> `$outputName`"

            if (Namespaces[outputContainer.namespace].supportsFieldDescription()) {
                text += "\n"
                text += "**Types:** `${mappedDesc.localiseFieldDesc()}`"
            }

            text
        }
        pages.add(page)
    }

    return pages
}

fun methodMatchesToPages(
    outputContainer: MappingsContainer,
    matches: List<Pair<Method, Method>>
): List<String> {
    val pages = mutableListOf<String>()

    for (match in matches.chunked(PAGE_SIZE)) {
        val page = match.joinToString("\n\n") {
            val (input, output) = it
            val mappedDesc = output.getMappedDesc(outputContainer)

            val inputName = input.mappedName ?: input.optimumName
            val outputName = output.mappedName ?: output.optimumName

            var text = "**Method:** `$inputName` -> `$outputName`"

            text += "\n" +
                "**Description:** `$mappedDesc`"

            text
        }
        pages.add(page)
    }

    return pages
}
