/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

@file:Suppress("StringLiteralDuplication", "UNUSED_PARAMETER")

// UNUSED_PARAMETER is suppressed in order to make code more abstract :pineapple:

package dev.kordex.modules.func.mappings.utils

import dev.kordex.modules.func.mappings.utils.linkie.mapIfNotNullOrNotEquals
import dev.kordex.modules.func.mappings.utils.linkie.stringPairs
import me.shedaniel.linkie.*
import me.shedaniel.linkie.namespaces.MojangHashedNamespace
import me.shedaniel.linkie.utils.*

private const val PAGE_SIZE = 3

private typealias ClassResults = QueryResult<MappingsContainer, ClassResultList>
private typealias Matches<T> = Map<T, T>

/** Given a set of result classes, format them into a list of pages for the paginator. **/
fun classesToPages(
	namespace: Namespace,
	classes: List<Class>,
): List<Pair<String, String>> {
	val pages = mutableListOf<Pair<String, String>>()

	classes.chunked(PAGE_SIZE).forEach { result ->
		val shortPage = result.joinToString("\n\n") { clazz ->
			buildString {
				append("**Class:** `${clazz.optimumName}`\n")

				val (clientName, serverName) = clazz.obfName.stringPairs()

				if (clientName != null && clientName.isNotEmpty()) {
					if (serverName == null) {
						append("**Name:** `$clientName` -> ")
					} else {
						append("**Client:** `$clientName` -> ")
					}

					append(
						"`${clazz.intermediaryName}`" +
							(
								clazz.mappedName.mapIfNotNullOrNotEquals(clazz.intermediaryName) { name ->
									" -> `$name`"
								} ?: ""
								)
					)
				}

				if (serverName != null && serverName.isNotEmpty()) {
					if (clientName != null) {
						append("\n")
					}

					append("**Server:** `$serverName` -> ")

					append(
						"`${clazz.intermediaryName}`" +
							(
								clazz.mappedName.mapIfNotNullOrNotEquals(clazz.intermediaryName) { name ->
									" -> `$name`"
								} ?: ""
								)
					)
				}
			}.trimEnd('\n')
		}

		val longPage = result.joinToString("\n\n") { clazz ->
			buildString {
				append("**Class:** `${clazz.optimumName}`\n")

				val (clientName, serverName) = clazz.obfName.stringPairs()

				if (clientName != null && clientName.isNotEmpty()) {
					if (serverName == null) {
						append("**Name:** `$clientName` -> ")
					} else {
						append("**Client:** `$clientName` -> ")
					}

					append(
						"`${clazz.intermediaryName}`" +
							(
								clazz.mappedName.mapIfNotNullOrNotEquals(clazz.intermediaryName) { name ->
									" -> `$name`"
								} ?: ""
								)
					)
				}

				if (serverName != null && serverName.isNotEmpty()) {
					if (clientName != null) {
						append("\n")
					}

					append("**Server:** `$serverName` -> ")

					append(
						"`${clazz.intermediaryName}`" +
							(
								clazz.mappedName.mapIfNotNullOrNotEquals(clazz.intermediaryName) { name ->
									" -> `$name`"
								} ?: ""
								)
					)
				}

				append("\n")

				if (namespace.supportsAT()) {
					append(
						"**Access Transformer:** `public " +
							clazz.intermediaryName.replace('/', '.') +
							"`"
					)
				} else if (namespace.supportsAW()) {
					append(
						"\n**Access Widener:** `accessible class ${clazz.optimumName}`"
					)
				}
			}.trimEnd('\n')
		}

		pages.add(Pair(shortPage, longPage))
	}

	return pages
}

/** Given a set of result classes, format them into a list of pages for the paginator. **/
fun classesToPages(
	namespace: Namespace,
	queryResult: ClassResults,
) =
	classesToPages(namespace, queryResult.map { it.map { inner -> inner.value }.toList() }.value)

/**
 * A convenience function variable for having an unused MappingsContainer argument.
 * This is to allow a more general function to be used for all queries.
 */
val classesToPages = { namespace: Namespace, _: MappingsContainer, classes: ClassResults, _: Boolean ->
	classesToPages(namespace, classes)
}

/** Given a set of result fields, format them into a list of pages for the paginator. **/
fun fieldsToPages(
	namespace: Namespace,
	mappings: MappingsContainer,
	fields: List<MemberEntry<Field>>,
	mapDescriptors: Boolean = true,
): List<Pair<String, String>> {
	val pages = mutableListOf<Pair<String, String>>()

	fields.chunked(PAGE_SIZE).forEach { result ->
		val shortPage = result.joinToString("\n\n") {
			val (clazz, field) = it

			val desc = if (mapDescriptors) {
				field.getMappedDesc(mappings)
			} else {
				field.intermediaryDesc
			}

			buildString {
				append("**Field:** `${clazz.optimumName}::${field.optimumName}`\n")

				val (clientName, serverName) = field.obfName.stringPairs()

				if (!clientName.isNullOrEmpty()) {
					if (!serverName.isNullOrEmpty()) {
						append("**Name:** `$clientName` -> ")
					} else {
						append("**Client:** `$clientName` -> ")
					}

					append(
						"`${field.intermediaryName}`" +
							(
								field.mappedName.mapIfNotNullOrNotEquals(field.intermediaryName) { name ->
									" -> `$name`"
								} ?: ""
								)
					)
				}

				if (!serverName.isNullOrEmpty()) {
					if (!clientName.isNullOrEmpty()) {
						append("\n")
					}

					append("**Server:** `$serverName` -> ")

					append(
						"`${field.intermediaryName}`" +
							(
								field.mappedName.mapIfNotNullOrNotEquals(field.intermediaryName) { name ->
									" -> `$name`"
								} ?: ""
								)
					)
				}

				if (namespace.supportsFieldDescription()) {
					append("\n")
					append("**Types:** `${desc.localiseFieldDesc()}`")
				}
			}
		}

		val longPage = result.joinToString("\n\n") {
			val (clazz, field) = it
			val mappedDesc = field.getMappedDesc(mappings)

			buildString {
				append("**Field:** `${clazz.optimumName}::${field.optimumName}`\n")

				val (clientName, serverName) = field.obfName.stringPairs()

				if (!clientName.isNullOrEmpty()) {
					if (!serverName.isNullOrEmpty()) {
						append("**Name:** `$clientName` -> ")
					} else {
						append("**Client:** `$clientName` -> ")
					}

					append(
						"`${field.intermediaryName}`" +
							(
								field.mappedName.mapIfNotNullOrNotEquals(field.intermediaryName) { name ->
									" -> `$name`"
								} ?: ""
								)
					)
				}

				if (!serverName.isNullOrEmpty()) {
					if (!clientName.isNullOrEmpty()) {
						append("\n")
					}

					append("**Server:** `$serverName` -> ")

					append(
						"`${field.intermediaryName}`" +
							(
								field.mappedName.mapIfNotNullOrNotEquals(field.intermediaryName) { name ->
									" -> `$name`"
								} ?: ""
								)
					)
				}

				if (namespace.supportsFieldDescription()) {
					append("\n")
					append("**Types:** `${mappedDesc.localiseFieldDesc()}`")
				}

				append("\n")

				if (namespace.supportsMixin()) {
					append("\n")

					append(
						"**Mixin Target:** `" +
							"L${clazz.optimumName};" +
							field.optimumName +
							":" +
							mappedDesc +
							"`"
					)
				}

				if (namespace.supportsAT()) {
					append("\n")

					append("**Access Transformer:** `${field.intermediaryName} # ${field.optimumName}`")
				} else if (namespace.supportsAW()) {
					append("\n")

					append(
						"**Access Widener:** `accessible field ${clazz.optimumName} ${field.optimumName} $mappedDesc`"
					)
				}
			}.trimEnd('\n')
		}

		pages.add(Pair(shortPage, longPage))
	}

	return pages
}

/** Given a set of result fields, format them into a list of pages for the paginator. **/
fun fieldsToPages(
	namespace: Namespace,
	mappings: MappingsContainer,
	queryResult: QueryResult<MappingsContainer, FieldResultList>,
	mapDescriptors: Boolean = true,
) =
	fieldsToPages(
		namespace,
		mappings,
		queryResult.map { it.map { inner -> inner.value }.toList() }.value,
		mapDescriptors
	)

/** Given a set of result methods, format them into a list of pages for the paginator. **/
fun methodsToPages(
	namespace: Namespace,
	mappings: MappingsContainer,
	methods: List<MemberEntry<Method>>,
	mapDescriptors: Boolean = true,
): List<Pair<String, String>> {
	val pages = mutableListOf<Pair<String, String>>()

	methods.chunked(PAGE_SIZE).forEach { result ->
		val shortPage = result.joinToString("\n\n") {
			val (clazz, method) = it
			buildString {
				append("**Method:** `${clazz.optimumName}::${method.optimumName}`\n")

				val (clientName, serverName) = method.obfName.stringPairs()

				if (!clientName.isNullOrEmpty()) {
					if (!serverName.isNullOrEmpty()) {
						append("**Name:** `$clientName` -> ")
					} else {
						append("**Client:** `$clientName` -> ")
					}

					append(
						"`${method.intermediaryName}`" +
							(
								method.mappedName.mapIfNotNullOrNotEquals(method.intermediaryName) { name ->
									" -> `$name`"
								} ?: ""
								)
					)
				}

				if (!serverName.isNullOrEmpty()) {
					if (!clientName.isNullOrEmpty()) {
						append("\n")
					}

					append("**Server:** `$serverName` -> ")

					append(
						"`${method.intermediaryName}`" +
							(
								method.mappedName.mapIfNotNullOrNotEquals(method.intermediaryName) { name ->
									" -> `$name`"
								} ?: ""
								)
					)
				}
			}.trimEnd('\n')
		}

		val longPage = result.joinToString("\n\n") {
			val (clazz, method) = it
			val desc = if (mapDescriptors) {
				method.getMappedDesc(mappings)
			} else {
				method.intermediaryDesc
			}

			buildString {
				append("**Method:** `${clazz.optimumName}::${method.optimumName}`\n")

				val (clientName, serverName) = method.obfName.stringPairs()

				if (clientName != null && clientName.isNotEmpty()) {
					if (serverName == null) {
						append("**Name:** `$clientName` -> ")
					} else {
						append("**Client:** `$clientName` -> ")
					}

					append(
						"`${method.intermediaryName}`" +
							(
								method.mappedName.mapIfNotNullOrNotEquals(method.intermediaryName) { name ->
									" -> `$name`"
								} ?: ""
								)
					)
				}

				if (serverName != null && serverName.isNotEmpty()) {
					if (clientName != null) {
						append("\n")
					}

					append("**Server:** `$serverName` -> ")

					append(
						"`${method.intermediaryName}`" +
							(
								method.mappedName.mapIfNotNullOrNotEquals(method.intermediaryName) { name ->
									" -> `$name`"
								} ?: ""
								)
					)
				}

				append("\n")

				if (namespace.supportsMixin()) {
					append("\n")

					append(
						"**Mixin Target** `" +
							"L${clazz.optimumName};" +
							method.optimumName +
							desc +
							"`"
					)
				}

				if (namespace.supportsAT()) {
					append("\n")

					append(
						"**Access Transformer** `public" + clazz.optimumName.replace('/', '.') +
							method.intermediaryName +
							desc +
							" # ${method.optimumName}`"
					)
				} else if (namespace.supportsAW()) {
					append("\n")

					append("**Access Widener** `accessible method ${clazz.optimumName} ${method.optimumName} $desc`")
				}
			}.trimEnd('\n')
		}

		pages.add(Pair(shortPage, longPage))
	}

	return pages
}

/** Given a set of result methods, format them into a list of pages for the paginator. **/
fun methodsToPages(
	namespace: Namespace,
	mappings: MappingsContainer,
	queryResult: QueryResult<MappingsContainer, MethodResultList>,
	mapDescriptors: Boolean = true,
) = methodsToPages(
	namespace,
	mappings,
	queryResult.map { it.map { inner -> inner.value }.toList() }.value,
	mapDescriptors
)

/** Given a set of class mapping matches, format them into a list of pages for the paginator. **/
fun classMatchesToPages(
	outputContainer: MappingsContainer,
	matches: List<Pair<Class, Class>>,
): List<String> {
	val pages = mutableListOf<String>()

	for (match in matches.chunked(PAGE_SIZE)) {
		val text = match.joinToString("\n\n") { (input, output) ->

			val inputName = input.mappedName ?: input.optimumName

			val outputName = if (outputContainer.namespace.toNamespace() == MojangHashedNamespace) {
				// Because of requests and how hashed is often used for its hashes rather than the names,
				// we call from "intermediary" instead of mapped.
				output.intermediaryName
			} else {
				output.mappedName ?: output.optimumName
			}

			"**Class:** `$inputName` -> `$outputName`"
		}
		pages.add(text)
	}

	return pages
}

/** Convienence function for making code more generalized. */
val classMatchesToPages = { outputContainer: MappingsContainer, classMatches: Matches<Class> ->
	classMatchesToPages(outputContainer, classMatches.toList())
}

/** Given a set of field mapping matches, format them into a list of pages for the paginator. **/
fun fieldMatchesToPages(
	outputContainer: MappingsContainer,
	matches: List<Pair<MemberEntry<Field>, MemberEntry<Field>>>,
): List<String> {
	val pages = mutableListOf<String>()

	for (match in matches.chunked(PAGE_SIZE)) {
		val page = match.joinToString("\n\n") {
			val (inputClass, inputField) = it.first
			val (outputClass, outputField) = it.second
			val mappedDesc = outputField.getMappedDesc(outputContainer)

			val inputName = inputField.mappedName ?: inputField.optimumName

			val outputName = if (outputContainer.namespace.toNamespace() == MojangHashedNamespace) {
				outputField.intermediaryName
			} else {
				outputField.mappedName ?: outputField.optimumName
			}

			val inputClassName = inputClass.mappedName ?: inputClass.optimumName

			val outputClassName = if (outputContainer.namespace.toNamespace() == MojangHashedNamespace) {
				outputClass.intermediaryName
			} else {
				outputClass.mappedName ?: outputClass.optimumName
			}

			buildString {
				append("**Field:** `$inputClassName::$inputName` -> `$outputClassName::$outputName`")

				val namespace = if (outputContainer.namespace == "hashed-mojmap") {
					// thanks linkie you're ruining everything
					MojangHashedNamespace
				} else {
					Namespaces[outputContainer.namespace]
				}

				if (namespace.supportsFieldDescription()) {
					append("\n")
					append("**Types:** `${mappedDesc.localiseFieldDesc()}`")
				}
			}
		}
		pages.add(page)
	}

	return pages
}

/** Convenience function for making code more generalized. */
val fieldMatchesToPages = { outputContainer: MappingsContainer, fieldMatches: Matches<MemberEntry<Field>> ->
	fieldMatchesToPages(outputContainer, fieldMatches.toList())
}

/** Given a set of method mapping matches, format them into a list of pages for the paginator. **/
fun methodMatchesToPages(
	outputContainer: MappingsContainer,
	matches: List<Pair<MemberEntry<Method>, MemberEntry<Method>>>,
): List<String> {
	val pages = mutableListOf<String>()

	for (match in matches.chunked(PAGE_SIZE)) {
		val page = match.joinToString("\n\n") {
			val (inputClass, inputMethod) = it.first
			val (outputClass, outputMethod) = it.second
			val mappedDesc = outputMethod.getMappedDesc(outputContainer)

			val inputName = inputMethod.mappedName ?: inputMethod.optimumName

			val outputName = if (outputContainer.namespace.toNamespace() == MojangHashedNamespace) {
				outputMethod.intermediaryName
			} else {
				outputMethod.mappedName ?: outputMethod.optimumName
			}

			val inputClassName = inputClass.mappedName ?: inputClass.optimumName

			val outputClassName = if (outputContainer.namespace.toNamespace() == MojangHashedNamespace) {
				outputClass.intermediaryName
			} else {
				outputClass.mappedName ?: outputClass.optimumName
			}

			"**Method:** `$inputClassName::$inputName` -> `$outputClassName::$outputName`" +
				"\n**Description:** `$mappedDesc`"
		}
		pages.add(page)
	}

	return pages
}

/** Convienence function for making code more generalized. */
val methodMatchesToPages = { outputContainer: MappingsContainer, methodMatches: Matches<MemberEntry<Method>> ->
	methodMatchesToPages(outputContainer, methodMatches.toList())
}

/** Attempt to get an obfuscated name from three possible states. */
val Obf.preferredName: String?
	get() = merged ?: client ?: server
