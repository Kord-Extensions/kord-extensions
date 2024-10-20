/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.pagination.pages

import dev.kord.rest.builder.message.EmbedBuilder
import dev.kordex.core.ExtensibleBot
import dev.kordex.core.i18n.generated.CoreTranslations
import dev.kordex.core.i18n.types.Key
import dev.kordex.core.koin.KordExKoinComponent
import dev.kordex.core.pagination.builders.PageMutator
import dev.kordex.core.utils.capitalizeWords
import dev.kordex.core.utils.textOrNull
import org.koin.core.component.inject
import java.util.*
import kotlin.math.ceil
import kotlin.math.roundToInt

/**
 * Representation of a single paginator page. You can extend this to customise it if you wish!
 *
 * @param builder Embed builder callable for building the page's embed
 */
public open class Page(
	public open val builder: suspend EmbedBuilder.() -> Unit,
) : KordExKoinComponent {
	/** Current instance of the bot. **/
	public open val bot: ExtensibleBot by inject()

	/** Create an embed builder for this page. **/
	public open suspend fun build(
		locale: Locale,
		pageNum: Int,
		chunkSize: Int,
		pages: Int,
		group: Key?,
		groupIndex: Int,
		groups: Int,
		shouldMutateFooter: Boolean = true,
		shouldPutFooterInDescription: Boolean = false,
		mutator: PageMutator? = null,
	): suspend EmbedBuilder.() -> Unit = {
		builder()

		if (mutator != null) {
			mutator(this, this@Page)
		}

		if (shouldMutateFooter) {
			val curFooterText = footer?.textOrNull()

			val footerText = buildString {
				if (pages > 1) {
					if (chunkSize > 1) {
						append(
							CoreTranslations.Paginator.Footer.Page.chunked
								.withLocale(locale)
								.translate(
									ceil((pageNum + 1).div(chunkSize.toFloat())).roundToInt(), // Current page
									ceil(pages.div(chunkSize.toFloat())).roundToInt(), // Total pages
									pages, // Total chunks
								)
						)
					} else {
						append(
							CoreTranslations.Paginator.Footer.page
								.withLocale(locale)
								.translate(
									pageNum + 1,
									pages
								)
						)
					}
				}

				if (group != null || groups > 2) {
					if (isNotBlank()) {
						append(" • ")
					}

					if (group == null || group.key.isBlank()) {
						append(
							CoreTranslations.Paginator.Footer.group
								.withLocale(locale)
								.translate(
									groupIndex + 1,
									groups
								)
						)
					} else {
						val groupName = group
							.withLocale(locale)
							.translate()
							.capitalizeWords(locale)

						append("$groupName (${groupIndex + 1}/$groups)")
					}
				}

				if (!curFooterText.isNullOrEmpty()) {
					if (isNotBlank()) {
						append(" • ")
					}

					append(curFooterText)
				}
			}

			if (shouldPutFooterInDescription) {
				description = footerText
			} else {
				val curFooterIcon = footer?.icon

				footer {
					icon = curFooterIcon

					text = footerText
				}
			}
		}
	}
}
