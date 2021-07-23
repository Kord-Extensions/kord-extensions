package com.kotlindiscord.kord.extensions.pagination.pages

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.i18n.TranslationsProvider
import com.kotlindiscord.kord.extensions.utils.capitalizeWords
import dev.kord.common.Color
import dev.kord.rest.builder.message.EmbedBuilder
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.*

/**
 * Representation of a single paginator page. You can subclass this to customize it if you wish!
 *
 * @param description Required: Page body.
 * @param title Optional: Embed title.
 * @param author Optional: Embed author name.
 * @param authorIcon Optional: Embed author icon.
 * @param authorUrl Optional: Embed author url.
 * @param color Optional: Embed color.
 * @param footer Optional: Embed footer text
 * @param footerIcon Optional: Embed footer icon
 * @param image Optional: Embed image
 * @param thumbnail Optional: Embed thumbnail
 * @param url Optional: Embed URL
 * @param bundle Optional: Translations bundle for group names
 */
public open class Page(
    public open val description: String,
    public open val title: String? = null,
    public open val author: String? = null,
    public open val authorIcon: String? = null,
    public open val authorUrl: String? = null,
    public open val color: Color? = null,
    public open val footer: String? = null,
    public open val footerIcon: String? = null,
    public open val image: String? = null,
    public open val thumbnail: String? = null,
    public open val url: String? = null,
    public open val bundle: String? = null,
) : KoinComponent {
    /** Current instance of the bot. **/
    public open val bot: ExtensibleBot by inject()

    /** Translations provider, for retrieving translations. **/
    public val translationsProvider: TranslationsProvider by inject()

    /** Whether to try to fill the author property. **/
    public open val anyAuthor: Boolean = listOf(author, authorIcon, authorUrl).any { it != null }

    /** Whether to try to fill the footer property. **/
    public open val anyFooter: Boolean = footer != null

    /** Create an embed builder for this page. **/
    public open fun build(
        locale: Locale,
        pageNum: Int,
        pages: Int,
        group: String?,
        groupIndex: Int,
        groups: Int
    ): EmbedBuilder.() -> Unit = {
        this.description = this@Page.description
        this.title = this@Page.title

        if (anyAuthor) {
            author {
                name = this@Page.author
                icon = authorIcon
                url = authorUrl
            }
        }

        this.color = this@Page.color

        footer {
            icon = footerIcon
            text = ""

            if (pages > 1) {
                text += translationsProvider.translate(
                    "paginator.footer.page",
                    locale,
                    replacements = arrayOf(pageNum + 1, pages)
                )
            }

            if (group != null && group.isNotBlank() || groups > 2) {
                if (text.isNotBlank()) {
                    text += " • "
                }

                text += if (group.isNullOrBlank()) {
                    translationsProvider.translate(
                        "paginator.footer.group",
                        locale,
                        replacements = arrayOf(groupIndex + 1, groups)
                    )
                } else {
                    val groupName = translationsProvider.translate(
                        group, locale, bundle
                    ).capitalizeWords(locale)

                    "$groupName (${groupIndex + 1}/$groups)"
                }
            }

            if (anyFooter) {
                if (text.isNotBlank()) {
                    text += " • "
                }

                text += this@Page.footer
            }
        }

        this.image = this@Page.image

        if (this@Page.thumbnail != null) {
            thumbnail {
                this.url = this@Page.thumbnail!!
            }
        }

        this.url = this@Page.url
    }
}
