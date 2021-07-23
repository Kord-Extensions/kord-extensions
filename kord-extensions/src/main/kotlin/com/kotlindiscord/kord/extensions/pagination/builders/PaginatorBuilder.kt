package com.kotlindiscord.kord.extensions.pagination.builders

import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.pagination.pages.Page
import com.kotlindiscord.kord.extensions.pagination.pages.Pages
import dev.kord.core.entity.ReactionEmoji
import dev.kord.core.entity.User
import java.util.*

/**
 * Wrapping builder for easily creating paginators using DSL functions defined in the context classes.
 *
 * @param extension Extension the paginator is being created for
 * @param locale Locale to use for the paginator
 * @param defaultGroup Default page group, if any
 */
public class PaginatorBuilder(
    public val extension: Extension,
    public var locale: Locale? = null,
    public val defaultGroup: String = ""
) {
    /** Pages container object. **/
    public val pages: Pages = Pages(defaultGroup)

    /** Paginator owner, if only one person should be able to interact. **/
    public var owner: User? = null

    /** Paginator timeout, in seconds. When elapsed, the paginator will be destroyed. **/
    public var timeoutSeconds: Long? = null

    /** Whether to keep the paginator content on Discord when the paginator is destroyed. **/
    public var keepEmbed: Boolean = true

    /** Alternative switch button emoji, if needed. **/
    public var switchEmoji: ReactionEmoji? = null

    /** Translations bundle to use for page groups, if any. **/
    public var bundle: String? = null

    /** Add a page to [pages], using the default group. **/
    public fun page(page: Page): Unit = pages.addPage(page)

    /** Add a page to [pages], using the given group. **/
    public fun page(group: String, page: Page): Unit = pages.addPage(group, page)
}
