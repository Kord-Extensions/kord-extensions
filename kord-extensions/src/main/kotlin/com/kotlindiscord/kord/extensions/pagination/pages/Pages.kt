package com.kotlindiscord.kord.extensions.pagination.pages

/**
 * Class representing a set of pages in a paginator. You can subclass this to customize it if you wish!
 *
 * @param defaultGroup Default page group, if you have more than one.
 */
public open class Pages(public open var defaultGroup: String = "") {
    /** All groups of pages stored in this class. **/
    public open val groups: LinkedHashMap<String, MutableList<Page>> = linkedMapOf()

    /** Number of pages in a single group - this *must* be the same for all groups! **/
    public open val size: Int get() = groups.values.first().size

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

        if (!groups.values.all { it.size == size }) {
            throw IllegalArgumentException(
                "Invalid pages supplied: Not all groups have the same number of pages"
            )
        }
    }
}
