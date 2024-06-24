package types

import dev.yumi.gradle.licenser.api.comment.CStyleHeaderComment
import dev.yumi.gradle.licenser.api.comment.HeaderComment

class XmlStyleHeaderComment : HeaderComment {
	override fun readHeaderComment(source: String): HeaderComment.Result {
		val separator = this.extractLineSeparator(source)
		val firstBlock = source.split(separator.repeat(2)).first()
		val start = firstBlock.indexOf("<!--")

		if (start != 0) {
			val allWhitespacePrefixed = (0 until start).all { source[it] in arrayOf(' ', separator) }

			if (!allWhitespacePrefixed) {
				return HeaderComment.Result(0, 0, null, separator)
			}
		}

		val end = firstBlock.indexOf("-->") + 3

		if (start < 0 || end < 0) {
			return HeaderComment.Result(0, 0, null, separator)
		}

		val result: MutableList<String> = source.substring(start, end).split(separator).toMutableList()

		result.removeFirst()
		result.removeLast()

		return HeaderComment.Result(start, end, result.map { it.trimIndent() }, separator)
	}

	override fun writeHeaderComment(header: List<String>, separator: String): String =
		buildString {
			append("<!--$separator")

			header.forEach {
				append("\t$it$separator")
			}

			append("-->")
		}

	companion object {
		/**
		 * The implementation instance of this header comment type.
		 */
		val INSTANCE: XmlStyleHeaderComment = XmlStyleHeaderComment()
	}
}

// Testing function, ensure a match
fun main() {
	val xmlSource = """
		<!--
			This Source Code Form is subject to the terms of the Mozilla Public
			License, v. 2.0. If a copy of the MPL was not distributed with this
			file, You can obtain one at https://mozilla.org/MPL/2.0/.
		-->

		<script lang="ts">
		</script>
	""".trimIndent()

	val cSource = """
		/*
		 * This Source Code Form is subject to the terms of the Mozilla Public
		 * License, v. 2.0. If a copy of the MPL was not distributed with this
		 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
		 */

		package com.kotlindiscord.kord.extensions
	""".trimIndent()

	val xmlReader = XmlStyleHeaderComment()
	val cReader = CStyleHeaderComment()

	val xmlComment = xmlReader.readHeaderComment(xmlSource)
	val cComment = cReader.readHeaderComment(cSource)

	println("== XML style ==")
	println(xmlComment.existing?.joinToString("\n"))

	println()
	println("== C style ==")
	println(cComment.existing?.joinToString("\n"))

	println()

	if (xmlComment.existing == cComment.existing) {
		println("PASS: Comments match!")
	} else {
		println("FAIL: Comments don't match.")
	}
}
