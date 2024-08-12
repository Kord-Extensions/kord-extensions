/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

@file:Suppress("UndocumentedPublicFunction")

package dev.kordex.core.annotations

import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.validate

public fun docComment(comment: String): String = """
    |/**
    ${comment.split("\n").joinToString("\n") { "| * $it" }}
    | */
""".trimMargin() + "\n"

// public fun docComment(comment: String): String = """
// /**
// ${comment.split("\n").joinToString("\n") { " * $it" }}
// */
// """

@JvmName("function1")
public fun String.function(name: String): String = "${this}public fun $name("
public fun function(name: String): String = "public fun $name("

@JvmName("function2")
public fun String.function(name: String, receiver: String): String = "${this}public fun $receiver.$name(\n"
public fun function(name: String, receiver: String): String = "public fun $receiver.$name(\n"

public fun String.argument(name: String, type: String): String = "$this    $name: $type,\n"
public fun String.argument(name: String, type: String, default: String): String = "$this    $name: $type = $default,\n"

public fun String.returnsBrace(type: String): String = "$this): $type {\n"
public fun String.returnsStatement(type: String): String = "$this): $type =\n"

public fun String.line(line: String): String = "$this${line.prependIndent("    ")}\n"

public fun String.closeBrace(): String = "$this}"

public fun String.maybe(bool: Boolean, callback: (String) -> String): String = if (bool) {
	callback(this)
} else {
	this
}

public fun String.maybe(predicate: () -> Boolean, callback: (String) -> String): String = maybe(predicate(), callback)

public fun <T> Collection<T>.containsAny(vararg items: T): Boolean {
	items.forEach {
		if (this.contains(it)) return true
	}

	return false
}

public fun String?.orNull(): String? =
	if (this.isNullOrEmpty()) {
		null
	} else {
		this
	}

// Credit: https://stackoverflow.com/a/59737650
public fun <T> List<T>.permutations(): Set<List<T>> {
	if (this.isEmpty()) {
		return emptySet()
	}

	val permutationInstructions = this.toSet()
		.map { it to this.count { x -> x == it } }
		.fold(listOf(setOf<Pair<T, Int>>())) { acc, (value, valueCount) ->
			mutableListOf<Set<Pair<T, Int>>>().apply {
				for (set in acc) for (retainIndex in 0 until valueCount) add(set + (value to retainIndex))
			}
		}

	return mutableSetOf<List<T>>().also { outSet ->
		for (instructionSet in permutationInstructions) {
			outSet += this.toMutableList().apply {
				for ((value, retainIndex) in instructionSet) {
					repeat(retainIndex) { removeAt(indexOfFirst { it == value }) }
					repeat(count { it == value } - 1) { removeAt(indexOfLast { it == value }) }
				}
			}
		}
	}
}

public fun KSNode.validateIgnoringFunctions(): Boolean =
	this.validate { parent, current ->
		!(parent is KSFunctionDeclaration && current is KSDeclaration)
	}
