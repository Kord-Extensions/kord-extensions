/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.pagination.pages

import dev.kordex.core.i18n.EMPTY_KEY
import dev.kordex.core.i18n.types.Key

/**
 * Class representing a set of pages in a paginator. You can subclass this to customize it if you wish!
 *
 * @param defaultGroup Default page group, if you have more than one.
 */
public open class Pages(public open var defaultGroup: Key = EMPTY_KEY) {
	/** All groups of pages stored in this class. **/
	public open val groups: LinkedHashMap<Key, MutableList<Page>> = linkedMapOf()

	/** Add a page to the default group. **/
	public open fun addPage(page: Page): Unit = addPage(defaultGroup, page)

	/** Add a page to a given group. **/
	public open fun addPage(group: Key, page: Page) {
		groups[group] = groups[group] ?: mutableListOf()

		groups[group]!!.add(page)
	}

	/** Retrieve the page at the given index, from the default group. **/
	public open fun get(page: Int): Page = get(defaultGroup, page)

	/** Retrieve the page at the given index, from a given group. **/
	public open fun get(group: Key, page: Int): Page {
		if (groups[group] == null) {
			throw NoSuchElementException("No such group: $group")
		}

		val size = groups[group]!!.size

		if (page > size) {
			throw IndexOutOfBoundsException("Page out of range: $page ($size pages)")
		}

		return groups[group]!![page]
	}

	/** Check that this Pages object is valid, throwing if it isn't.. **/
	public open fun validate() {
		if (groups.isEmpty()) {
			throw IllegalArgumentException(
				"Invalid pages supplied: At least one page is required"
			)
		}
	}
}
