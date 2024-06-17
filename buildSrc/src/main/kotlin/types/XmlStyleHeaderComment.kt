package types

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

		val result: List<String> = source.substring(start, end).split(separator)

		return HeaderComment.Result(start, end, result, separator)
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
