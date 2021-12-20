/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.pagination.pages

/**
 * Class representing a set of pages in a paginator. You can subclass this to customize it if you wish!
 *
 * @param defaultGroup Default page group, if you have more than one.
 */
public open class Pages(public open var defaultGroup: String = "") {
    /** All groups of pages stored in this class. **/
    public open val groups: LinkedHashMap<String, MutableList<Page>> = linkedMapOf()

    /** Add a page to the default group. **/
    public open fun addPage(page: Page): Unit = addPage(defaultGroup, page)

    /** Add a page to a given group. **/
    public open fun addPage(group: String, page: Page) {
        groups[group] = groups[group] ?: mutableListOf()

        groups[group]!!.add(page)
    }

    /** Retrieve the page at the given index, from the default group. **/
    public open fun get(page: Int): Page = get(defaultGroup, page)

    /** Retrieve the page at the given index, from a given group. **/
    public open fun get(group: String, page: Int): Page {
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
