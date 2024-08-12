/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.parser

import dev.kordex.parser.tokens.NamedArgumentToken
import dev.kordex.parser.tokens.PositionalArgumentToken
import io.github.oshai.kotlinlogging.KotlinLogging

/**
 * String parser, tokenizing the input as requested by the function calls. Intended for command argument parsing, but
 * can be used for other things too if needed.
 *
 * This parser supports:
 *
 * * Keyword arguments at any position (key=value)
 * * Flag arguments at any position (--key value)
 * * Quoted arguments with spaces ("value value value")
 * * Unquoted arguments without spaces (value)
 *
 * The recommended workflow is as follows:
 *
 * 1. Call [parseNamed] to parse out the keyword/flag arguments, removing them from the [cursor]
 * 2. Call [parseNext] as required to parse out single or quoted tokens sequentially
 * 3. Use the other functions to parse in more specific ways
 *
 * @param input Input string to parse.
 */
@Suppress("StringLiteralDuplication")  // That'll happen in a parser like this
public open class StringParser(public open val input: String) {
	private val logger = KotlinLogging.logger { }

	/**
	 * Cursor object, representing the current parsing state. Initially, this will contain a [Cursor] that iterates
	 * over the entire [input], but functions (eg [parseNamed]) may reassign it to change the iteration target.
	 */
	public var cursor: Cursor = Cursor(input)

	/** Returns `true` if the [cursor] has more parsing to do. **/
	public val hasNext: Boolean get() = cursor.hasNext

	/**
	 * Parse all of the flag and keyword arguments out of the [cursor], creating a new [Cursor] containing only the
	 * positional arguments and returning a list of parsed [NamedArgumentToken]s.
	 *
	 * **Note:** This function reassigns the [cursor] property!
	 */
	public fun parseNamed(): List<NamedArgumentToken> {
		val tokens: MutableList<NamedArgumentToken> = mutableListOf()

		val buffer = StringBuilder()
		val outputBuffer = StringBuilder()

		var isQuoted = false

		var isFlag = false
		var isFlagValue = false
		var flagName = ""

		var isKeyword = false
		var keywordName = ""

		@Suppress("LoopWithTooManyJumpStatements")  // Tell me I suck, why don't you
		while (cursor.hasNext) {
			val char = cursor.next()

			logger.trace { "Character: $char" }

			val canBeQuoted = !isQuoted &&
				(!(isFlag && !isFlagValue) || isKeyword)

			if (char == '"' && buffer.isEmpty() && canBeQuoted) {
				// Nothing in the buffer, opening quote - ignore spaces as we continue
				logger.trace { "  Marking as quoted." }

				isQuoted = true
				continue
			}

			if (char == '-' && cursor.peekNext() == '-' && buffer.isEmpty() && !isFlag && !isKeyword) {
				// This is a flag, --key value type deal
				logger.trace { "  Marking as flag." }

				cursor.next()
				isFlag = true
				continue
			}

			if (char == '=' && buffer.isNotEmpty() && !isKeyword && !isFlag) {
				// Keyword pair, key=value
				logger.trace { "  Marking as keyword pair." }

				keywordName = buffer.toString()
				buffer.clear()

				isKeyword = true
				continue
			}

			if (char == '\\' && cursor.peekNext() == '"' && isQuoted) {
				// Escaped quote, only handle if it's in a quoted argument though
				logger.trace { "  Escaped quote." }

				buffer.append('"')
				cursor.next()
				continue
			}

			if (char == '"' && isQuoted) {
				// We're at the end of this part of the token
				logger.trace { "  Reached quoted end." }

				if (isFlagValue) {
					// Flag names can't have spaces
					logger.trace { "    Flag value detected." }
					logger.trace { "" }

					tokens.add(NamedArgumentToken(flagName, buffer.toString()))

					flagName = ""

					isFlag = false
					isFlagValue = false
				} else if (isKeyword) {
					// Keyword names can't have spaces either
					logger.trace { "    Keyword value detected." }
					logger.trace { "" }

					tokens.add(NamedArgumentToken(keywordName, buffer.toString()))

					keywordName = ""

					isKeyword = false
				} else {
					// Not a flag/keyword value, so we're at the end of this token
					logger.trace { "    Token end detected." }
					logger.trace { "" }

					outputBuffer.append("\"$buffer\"")
					cursor.skipWhitespace()
				}

				buffer.clear()
				isQuoted = false

				continue
			}

			if (char == ' ' && !isQuoted) {
				// Not quoted, so we're at the end of this part of the token
				logger.trace { "  Whitespace detected." }

				if (isFlag) {
					// Could be a flag name or value, they're space-separated

					if (!isFlagValue) {
						logger.trace { "    Flag name detected." }
						logger.trace { "" }

						flagName = buffer.toString()
						isFlagValue = true
					} else {
						logger.trace { "    Flag value detected." }
						logger.trace { "" }

						tokens.add(NamedArgumentToken(flagName, buffer.toString()))
						cursor.skipWhitespace()

						flagName = ""

						isFlag = false
						isFlagValue = false
					}
				} else if (isKeyword) {
					// This is a keyword value
					logger.trace { "    Keyword value detected." }
					logger.trace { "" }

					tokens.add(NamedArgumentToken(keywordName, buffer.toString()))
					cursor.skipWhitespace()

					keywordName = ""

					isKeyword = false
				} else {
					// Not a flag/keyword value, so we're at the end of this token
					logger.trace { "    Token end detected." }
					logger.trace { "" }

					outputBuffer.append("$buffer ")
//                    cursor.skipWhitespace()
				}

				buffer.clear()
				continue
			}

			buffer.append(char)
			logger.trace { "  Adding: \"$buffer\" + '$char'" }
		}

		if (buffer.isNotEmpty()) {
			logger.trace { "" }
			logger.trace { "Buffer's not empty yet." }

			if (isFlag) {
				// Could be a flag name or value, they're space-separated

				if (!isFlagValue) {
					logger.trace { "  !! Flag name detected - this shouldn't happen!" }
				} else {
					logger.trace { "  Flag value detected." }

					tokens.add(NamedArgumentToken(flagName, buffer.toString()))
				}
			} else if (isKeyword) {
				// This is a keyword value
				logger.trace { "  Keyword value detected." }

				tokens.add(NamedArgumentToken(keywordName, buffer.toString()))
			} else {
				// Not a flag/keyword value, so we're at the end of this token
				logger.trace { "  End of token detected." }

				outputBuffer.append(buffer.toString())
			}
		}

		cursor = Cursor(outputBuffer.toString().trim())

		return tokens
	}

	/**
	 * Attempt to parse the next token and reset the cursor's index to what it was before parsing, before returning
	 * the result.
	 */
	public fun peekNext(): PositionalArgumentToken? {
		val curIndex = cursor.index
		val token = parseNext()

		cursor.index = curIndex

		return token
	}

	/**
	 * Attempt to parse a single or quoted positional argument token from the [cursor], returning it if there was a
	 * token, or `null` if there wasn't anything left to parse.
	 */
	public fun parseNext(): PositionalArgumentToken? {
		val buffer = StringBuilder()

		var token: PositionalArgumentToken? = null
		var isQuoted = false

		@Suppress("LoopWithTooManyJumpStatements")  // rude
		while (cursor.hasNext) {
			val char = cursor.next()

			if (char == '"' && buffer.isEmpty() && !isQuoted) {
				// Nothing in the buffer, opening quote - ignore spaces as we continue
				logger.trace { "  Marking as quoted." }

				isQuoted = true
				continue
			}

			if (char == '\\' && cursor.peekNext() == '"' && isQuoted) {
				// Escaped quote, only handle if it's in a quoted argument though
				logger.trace { "  Escaped quote." }

				buffer.append('"')
				cursor.next()
				continue
			}

			if (char == '"' && isQuoted) {
				// We're at the end of this part of the token
				logger.trace { "  Reached quoted end." }
				logger.trace { "    Token end detected." }
				logger.trace { "" }

				token = PositionalArgumentToken(buffer.toString())
				cursor.skipWhitespace()

				buffer.clear()

				break
			}

			if (char == ' ' && !isQuoted) {
				// Not quoted, so we're at the end of this part of the token
				logger.trace { "  Whitespace detected." }
				logger.trace { "    Token end detected." }
				logger.trace { "" }

				token = PositionalArgumentToken(buffer.toString())
				cursor.skipWhitespace()

				buffer.clear()

				break
			}

			buffer.append(char)
			logger.trace { "  Adding: \"$buffer\" + '$char'" }
		}

		if (buffer.isNotEmpty()) {
			logger.trace { "" }
			logger.trace { "Remaining buffer treated as positional token." }

			token = PositionalArgumentToken(buffer.toString())
		}

		return token
	}

	/** Consume whatever is left in the [cursor], setting it to the end of its input string. **/
	public fun consumeRemaining(): String = cursor.consumeRemaining()

	/**
	 * Consume characters from the [cursor] while the [predicate] returns `true`, and return those characters joined
	 * into a String. If the predicate fails on the first character, `null` will be returned instead.
	 *
	 * **Note:** Once this function has finished consuming characters, it will instruct the cursor to skip any
	 * immediate whitespace, which prepares it for normal token parsing - assuming there's anything left to parse.
	 */
	public fun consumeWhile(predicate: (Char) -> Boolean): String? {
		val result = cursor.consumeWhile(predicate)

		if (result != null) {
			cursor.skipWhitespace()
		}

		return result
	}

	/** Return whatever remains in the [cursor]'s string, without consuming it'. **/
	public fun peekRemaining(): String {
		val curIndex = cursor.index
		val result = cursor.consumeRemaining()

		cursor.index = curIndex

		return result
	}

	/**
	 * Collect characters from the [cursor] while the [predicate] returns `true`, and return those characters joined
	 * into a String. If the predicate fails on the first character, `null` will be returned instead. After
	 * characters have been collected, the cursor's index is reset, so the characters won't be consumed.
	 */
	public fun peekWhile(predicate: (Char) -> Boolean): String? {
		val curIndex = cursor.index
		val result = cursor.consumeWhile(predicate)

		cursor.index = curIndex

		return result
	}
}
