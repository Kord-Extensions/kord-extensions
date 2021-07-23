@file:Suppress("StringLiteralDuplication")

package com.kotlindiscord.kord.extensions.modules.extra.mappings.utils

import com.kotlindiscord.kord.extensions.modules.extra.mappings.utils.linkie.mapIfNotNullOrNotEquals
import com.kotlindiscord.kord.extensions.modules.extra.mappings.utils.linkie.stringPairs
import me.shedaniel.linkie.MappingsContainer
import me.shedaniel.linkie.Namespace
import me.shedaniel.linkie.getMappedDesc
import me.shedaniel.linkie.optimumName
import me.shedaniel.linkie.utils.*

private const val PAGE_SIZE = 3

/** Given a set of result classes, format them into a list of pages for the paginator. **/
fun classesToPages(
    namespace: Namespace,
    queryResult: QueryResult<MappingsContainer, ClassResultList>
): List<Pair<String, String>> {
    val pages = mutableListOf<Pair<String, String>>()
    val classes = queryResult.map { it.map { inner -> inner.value }.toList() }.value

    classes.chunked(PAGE_SIZE).forEach { result ->
        val shortPage = result.joinToString("\n\n") { clazz ->
            var text = ""

            text += "**Class:** `${clazz.optimumName}`\n"

            val (clientName, serverName) = clazz.obfName.stringPairs()

            if (clientName != null) {
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

            if (serverName != null) {
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

            if (clientName != null) {
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

            if (serverName != null) {
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

/** Given a set of result fields, format them into a list of pages for the paginator. **/
fun fieldsToPages(
    namespace: Namespace,
    mappings: MappingsContainer,
    queryResult: QueryResult<MappingsContainer, FieldResultList>
): List<Pair<String, String>> {
    val pages = mutableListOf<Pair<String, String>>()
    val fields = queryResult.map { it.map { inner -> inner.value }.toList() }.value

    fields.chunked(PAGE_SIZE).forEach { result ->
        val shortPage = result.joinToString("\n\n") {
            val (clazz, field) = it
            val mappedDesc = field.getMappedDesc(mappings)

            var text = ""

            text += "**Field:** `${clazz.optimumName}::${field.optimumName}`\n"

            val (clientName, serverName) = field.obfName.stringPairs()

            if (clientName != null) {
                if (serverName == null) {
                    text += "**Name:** `$clientName` -> "
                } else {
                    text += "**Client:** `$clientName` -> "
                }

                text += "`${field.intermediaryName}`" +
                    (field.mappedName.mapIfNotNullOrNotEquals(field.intermediaryName) { name ->
                        " -> `$name`"
                    } ?: "")
            }

            if (serverName != null) {
                if (clientName != null) {
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

            if (clientName != null) {
                if (serverName == null) {
                    text += "**Name:** `$clientName` -> "
                } else {
                    text += "**Client:** `$clientName` -> "
                }

                text += "`${field.intermediaryName}`" +
                    (field.mappedName.mapIfNotNullOrNotEquals(field.intermediaryName) { name ->
                        " -> `$name`"
                    } ?: "")
            }

            if (serverName != null) {
                if (clientName != null) {
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

/** Given a set of result methods, format them into a list of pages for the paginator. **/
fun methodsToPages(
    namespace: Namespace,
    mappings: MappingsContainer,
    queryResult: QueryResult<MappingsContainer, MethodResultList>
): List<Pair<String, String>> {
    val pages = mutableListOf<Pair<String, String>>()
    val methods = queryResult.map { it.map { inner -> inner.value }.toList() }.value

    methods.chunked(PAGE_SIZE).forEach { result ->
        val shortPage = result.joinToString("\n\n") {
            val (clazz, method) = it
            var text = ""

            text += "**Method:** `${clazz.optimumName}::${method.optimumName}`\n"

            val (clientName, serverName) = method.obfName.stringPairs()

            if (clientName != null) {
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

            if (serverName != null) {
                if (clientName != null) {
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

            if (clientName != null) {
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

            if (serverName != null) {
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
