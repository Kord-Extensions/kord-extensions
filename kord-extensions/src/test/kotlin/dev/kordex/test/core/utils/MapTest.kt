/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.test.core.utils

import dev.kordex.core.utils.MutableStringKeyedMap
import dev.kordex.core.utils.getOf
import dev.kordex.core.utils.getOfOrDefault
import dev.kordex.core.utils.getOfOrNull
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import kotlin.test.assertNull

/**
 * Tests for [Map] extension functions.
 */
class MapTest {
	/**
	 * Check that the typed map getters return the values expected of them.
	 */
	@Test
	@Execution(ExecutionMode.CONCURRENT)
	fun `check typed getters`() {
		val map: MutableStringKeyedMap<Any> = mutableMapOf(
			"string" to "value",
			"number" to 1
		)

		assertEquals(1, map.getOf("number"))
		assertEquals("value", map.getOf("string"))

		assertNull(map.getOfOrNull("missing"))
		assertEquals(2, map.getOrDefault("missing", 2))

		assertThrows<ClassCastException> { map.getOf<String>("number") }
		assertThrows<IllegalArgumentException> { map.getOf<Int>("missing") }

		assertEquals("two", map.getOfOrDefault("two", "two", true))
		assertEquals("two", map.getOf("two"))
		assertThrows<ClassCastException> { map.getOf<Int>("two") }

		assertEquals(2, map.getOfOrDefault("two", 2, true))
		assertEquals(2, map.getOf("two"))
		assertThrows<ClassCastException> { map.getOf<String>("two") }
	}
}
