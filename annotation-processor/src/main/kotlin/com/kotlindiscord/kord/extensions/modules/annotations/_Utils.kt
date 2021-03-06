@file:Suppress("UndocumentedPublicFunction")

package com.kotlindiscord.kord.extensions.modules.annotations

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
