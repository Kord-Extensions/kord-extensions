/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.parser.test

import dev.kordex.parser.StringParser
import dev.kordex.parser.tokens.NamedArgumentToken
import dev.kordex.parser.tokens.PositionalArgumentToken
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import kotlin.test.assertNull

const val EVERYTHING_INPUT = "single \"with quotes\" --key \"value with quotes\" name=value"
const val EVERYTHING_INPUT_NAMED_FIRST = "--key \"value with quotes\" name=value single \"with quotes\" "
const val NAMED_INPUT = "--one one two=two --three \"three three\" four=\"four four\""
const val SINGLE_INPUT = "one two three four five"
const val QUOTED_INPUT = "\"one one\" \"two two\" \"three three\" \"four four\" \"five five\""

const val NUMBERS = "12345"

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class StringParserTest {
	@Test
	@Execution(ExecutionMode.CONCURRENT)
	fun `Consuming - everything`() = runBlocking {
		val parser = StringParser(NUMBERS)
		val everything = parser.consumeRemaining()

		assertEquals(NUMBERS, everything)

		assertEquals("", parser.consumeRemaining())
		assertEquals(null, parser.parseNext())
	}

	@Test
	@Execution(ExecutionMode.CONCURRENT)
	fun `Consuming - predicates`() = runBlocking {
		val parser = StringParser(NUMBERS)

		val one = parser.consumeWhile { it == '1' }
		val two = parser.consumeWhile { it == '2' }
		val token = parser.parseNext()

		assertEquals("1", one)
		assertEquals("2", two)
		assertEquals(null, parser.consumeWhile { it == '2' })

		assertEquals("345", token?.data)

		assertEquals("", parser.consumeRemaining())
		assertEquals(null, parser.parseNext())
	}

	@Test
	@Execution(ExecutionMode.CONCURRENT)
	fun `Everything - natural order`() = runBlocking {
		val parser = StringParser(EVERYTHING_INPUT)

		val namedTokens: MutableList<NamedArgumentToken> = mutableListOf()
		val positionalTokens: MutableList<PositionalArgumentToken> = mutableListOf()

		namedTokens.addAll(parser.parseNamed())

		positionalTokens.add(parser.parseNext()!!)
		positionalTokens.add(parser.parseNext()!!)

		assertEquals(2, namedTokens.size) { "Parser should find two named tokens" }
		assertEquals(2, positionalTokens.size) { "Parser should find two positional tokens" }

		assertNull(parser.parseNext(), "Parser should not have anything else to parse.")

		assertEquals("key", namedTokens[0].name) { "Named 1: name = key" }
		assertEquals("value with quotes", namedTokens[0].data) { "Named 1: data = value with quotes" }

		assertEquals("name", namedTokens[1].name) { "Named 2: name = name" }
		assertEquals("value", namedTokens[1].data) { "Named 2: data = value" }

		assertEquals("single", positionalTokens[0].data) { "Positional 1: data = single" }
		assertEquals("with quotes", positionalTokens[1].data) { "Positional 2: data = with quotes" }
	}

	@Test
	@Execution(ExecutionMode.CONCURRENT)
	fun `Everything - named first`() = runBlocking {
		val parser = StringParser(EVERYTHING_INPUT_NAMED_FIRST)

		val namedTokens: MutableList<NamedArgumentToken> = mutableListOf()
		val positionalTokens: MutableList<PositionalArgumentToken> = mutableListOf()

		namedTokens.addAll(parser.parseNamed())

		positionalTokens.add(parser.parseNext()!!)
		positionalTokens.add(parser.parseNext()!!)

		assertEquals(2, namedTokens.size) { "Parser should find two named tokens" }
		assertEquals(2, positionalTokens.size) { "Parser should find two positional tokens" }

		assertNull(parser.parseNext(), "Parser should not have anything else to parse.")

		assertEquals("key", namedTokens[0].name) { "Named 1: name = key" }
		assertEquals("value with quotes", namedTokens[0].data) { "Named 1: data = value with quotes" }

		assertEquals("name", namedTokens[1].name) { "Named 2: name = name" }
		assertEquals("value", namedTokens[1].data) { "Named 2: data = value" }

		assertEquals("single", positionalTokens[0].data) { "Positional 1: data = single" }
		assertEquals("with quotes", positionalTokens[1].data) { "Positional 2: data = with quotes" }
	}

	@Test
	@Execution(ExecutionMode.CONCURRENT)
	fun `Single positional arguments`() = runBlocking {
		val parser = StringParser(SINGLE_INPUT)

		val positionalTokens: MutableList<PositionalArgumentToken> = mutableListOf()

		assertEquals(0, parser.parseNamed().size) { "Parser should find no named tokens" }

		positionalTokens.add(parser.parseNext()!!)
		positionalTokens.add(parser.parseNext()!!)
		positionalTokens.add(parser.parseNext()!!)
		positionalTokens.add(parser.parseNext()!!)
		positionalTokens.add(parser.parseNext()!!)

		assertEquals("one", positionalTokens[0].data) { "Positional 1: data = one" }
		assertEquals("two", positionalTokens[1].data) { "Positional 2: data = two" }
		assertEquals("three", positionalTokens[2].data) { "Positional 3: data = three" }
		assertEquals("four", positionalTokens[3].data) { "Positional 4: data = four" }
		assertEquals("five", positionalTokens[4].data) { "Positional 5: data = five" }

		assertNull(parser.parseNext(), "Parser should not have anything else to parse.")
	}

	@Test
	@Execution(ExecutionMode.CONCURRENT)
	fun `Quoted positional arguments`() = runBlocking {
		val parser = StringParser(QUOTED_INPUT)

		val positionalTokens: MutableList<PositionalArgumentToken> = mutableListOf()

		assertEquals(0, parser.parseNamed().size) { "Parser should find no named tokens" }

		positionalTokens.add(parser.parseNext()!!)
		positionalTokens.add(parser.parseNext()!!)
		positionalTokens.add(parser.parseNext()!!)
		positionalTokens.add(parser.parseNext()!!)
		positionalTokens.add(parser.parseNext()!!)

		assertEquals("one one", positionalTokens[0].data) { "Positional 1: data = one one" }
		assertEquals("two two", positionalTokens[1].data) { "Positional 2: data = two two" }
		assertEquals("three three", positionalTokens[2].data) { "Positional 3: data = three three" }
		assertEquals("four four", positionalTokens[3].data) { "Positional 4: data = four four" }
		assertEquals("five five", positionalTokens[4].data) { "Positional 5: data = five five" }

		assertNull(parser.parseNext(), "Parser should not have anything else to parse.")
	}

	@Test
	@Execution(ExecutionMode.CONCURRENT)
	fun `Named arguments only`() = runBlocking {
		val parser = StringParser(NAMED_INPUT)

		val namedTokens: MutableList<NamedArgumentToken> = mutableListOf()

		namedTokens.addAll(parser.parseNamed())

		assertEquals(4, namedTokens.size) { "Parser should find four named tokens" }

		assertEquals("one", namedTokens[0].name) { "Token 1: name = one" }
		assertEquals("one", namedTokens[0].data) { "Token 1: data = one" }

		assertEquals("two", namedTokens[1].name) { "Token 2: name = two" }
		assertEquals("two", namedTokens[1].data) { "Token 2: data = two" }

		assertEquals("three", namedTokens[2].name) { "Token 3: name = three" }
		assertEquals("three three", namedTokens[2].data) { "Token 3: data = three three" }

		assertEquals("four", namedTokens[3].name) { "Token 4: name = four" }
		assertEquals("four four", namedTokens[3].data) { "Token 4: data = four four" }

		assertNull(parser.parseNext(), "Parser should not have anything else to parse.")
	}
}
