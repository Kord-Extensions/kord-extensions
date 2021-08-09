package com.kotlindiscord.kord.extensions.pagination.pages

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.i18n.TranslationsProvider
import com.kotlindiscord.kord.extensions.utils.capitalizeWords
import com.kotlindiscord.kord.extensions.utils.textOrNull
import dev.kord.rest.builder.message.EmbedBuilder
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.*

/**
 * Representation of a single paginator page. You can extend this to customize it if you wish!
 *
 * @param bundle Optional: Translations bundle for group names
 * @param builder Embed builder callable for building the page's embed
 */
public open class Page(
    public open val bundle: String? = null,
    public open val builder: suspend EmbedBuilder.() -> Unit
) : KoinComponent {
    /** Current instance of the bot. **/
    public open val bot: ExtensibleBot by inject()

    /** Translations provider, for retrieving translations. **/
    public val translationsProvider: TranslationsProvider by inject()

    /** Create an embed builder for this page. **/
    public open suspend fun build(
        locale: Locale,
        pageNum: Int,
        pages: Int,
        group: String?,
        groupIndex: Int,
        groups: Int
    ): suspend EmbedBuilder.() -> Unit = {
        builder()

        val curFooterText = footer?.textOrNull()
        val curFooterIcon = footer?.icon

        footer {
            icon = curFooterIcon
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

            if (!curFooterText.isNullOrEmpty()) {
                if (text.isNotBlank()) {
                    text += " • "
                }

                text += curFooterText
            }
        }
    }
}
