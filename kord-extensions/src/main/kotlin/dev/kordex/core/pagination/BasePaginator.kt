/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.pagination

import dev.kord.core.Kord
import dev.kord.core.behavior.UserBehavior
import dev.kord.core.entity.ReactionEmoji
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.rest.builder.message.MessageBuilder
import dev.kord.rest.builder.message.embed
import dev.kordex.core.DISCORD_BLURPLE
import dev.kordex.core.ExtensibleBot
import dev.kordex.core.i18n.types.Key
import dev.kordex.core.koin.KordExKoinComponent
import dev.kordex.core.pagination.builders.PageTransitionCallback
import dev.kordex.core.pagination.pages.Page
import dev.kordex.core.pagination.pages.Pages
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import org.koin.core.component.inject
import java.util.*

/** Emoji used to jump to the first page. **/
public val FIRST_PAGE_EMOJI: ReactionEmoji.Unicode = ReactionEmoji.Unicode("⏮️")

/** Emoji used to jump to the previous page. **/
public val LEFT_EMOJI: ReactionEmoji.Unicode = ReactionEmoji.Unicode("⬅️")

/** Emoji used to jump to the next page. **/
public val RIGHT_EMOJI: ReactionEmoji.Unicode = ReactionEmoji.Unicode("➡️")

/** Emoji used to jump to the last page. **/
public val LAST_PAGE_EMOJI: ReactionEmoji.Unicode = ReactionEmoji.Unicode("⏭️")

/** Emoji used to destroy the paginator and delete the message. **/
public val DELETE_EMOJI: ReactionEmoji.Unicode = ReactionEmoji.Unicode("\uD83D\uDDD1️")

/** Emoji used to destroy the paginator without deleting the message. **/
public val FINISH_EMOJI: ReactionEmoji.Unicode = ReactionEmoji.Unicode("☑️")

/** Group switch emoji, counter-clockwise arrows icon. **/
public val SWITCH_EMOJI: ReactionEmoji.Unicode = ReactionEmoji.Unicode("\uD83D\uDD04")

/** Group switch emoji, information icon. **/
public val EXPAND_EMOJI: ReactionEmoji.Unicode = ReactionEmoji.Unicode("\u2139\uFE0F")

/**
 * Abstract class intended for building paginators.
 *
 * **Note:** This is going to be renamed - it's not ready for use yet!
 *
 * @param pages Pages object containing this paginator's pages
 * @param pages How many "pages" should be displayed at once, from 1 to 9
 * @param owner Optional paginator owner - setting this will prevent other users from interacting with the paginator
 * @param timeoutSeconds How long (in seconds) to wait before destroying the paginator, if needed
 * @param keepEmbed Set this to `false` to remove the paginator's message when it's destroyed
 * @param switchEmoji The `ReactionEmoji` to use for group switching
 * @param locale A Locale object for this pagination context, which defaults to the bot's default locale
 */
public abstract class BasePaginator(
	public open val pages: Pages,
	public open val chunkedPages: Int = 1,
	public open val owner: UserBehavior? = null,
	public open val timeoutSeconds: Long? = null,
	public open val keepEmbed: Boolean = true,
	public open val switchEmoji: ReactionEmoji = if (pages.groups.size == 2) EXPAND_EMOJI else SWITCH_EMOJI,
	public open val mutator: PageTransitionCallback? = null,

	locale: Locale? = null,
) : KordExKoinComponent {
	protected val logger: KLogger = KotlinLogging.logger {}

	/** Current instance of the bot. **/
	public val bot: ExtensibleBot by inject()

	/** Kord instance, backing the ExtensibleBot. **/
	public val kord: Kord by inject()

	/** Locale to use for translations. **/
	public open val localeObj: Locale = locale ?: bot.settings.i18nBuilder.defaultLocale

	/** What to do after the paginator times out. **/
	public val timeoutCallbacks: MutableList<suspend () -> Unit> = mutableListOf()

	/** Currently-displayed page index. **/
	public var currentPageNum: Int = 0

	/** Currently-displayed page group. **/
	public var currentGroup: Key = pages.defaultGroup

	/** Whether this paginator is currently active and processing events. **/
	public open var active: Boolean = true

	/** Set of all page groups. **/
	public open var allGroups: List<Key> = pages.groups.map { it.key }

	init {
		if (pages.groups.filterValues { it.isNotEmpty() }.isEmpty()) {
			error("Attempted to send a paginator with no pages in it")
		}
	}

	/** Currently-displayed page object. **/
	public open var currentPages: List<Page> = getChunk()

	public open fun getChunk(): List<Page> {
		val result: MutableList<Page> = mutableListOf()

		for (pageNum in currentPageNum until currentPageNum + chunkedPages) {
			@Suppress("TooGenericExceptionCaught")
			try {
				val page = pages.get(currentGroup, pageNum)

				result.add(page)
			} catch (_: NoSuchElementException) {
				break
			} catch (_: IndexOutOfBoundsException) {
				break
			}
		}

		return result
	}

	/** Builder that generates an embed for the paginator's current context. **/
	public open suspend fun MessageBuilder.applyPage() {
		val groupEmoji = if (pages.groups.size > 1) {
			currentGroup
		} else {
			null
		}

		currentPages.forEach {
			embed {
				logger.debug { "Building page: $it" }

				it.build(
					locale = localeObj,
					pageNum = currentPageNum,
					chunkSize = chunkedPages,
					pages = pages.groups[currentGroup]!!.size,
					group = groupEmoji,
					groupIndex = allGroups.indexOf(currentGroup),
					groups = allGroups.size,
					shouldMutateFooter = chunkedPages == 1,
					mutator = mutator?.pageMutator
				)()
			}
		}

		if (chunkedPages > 1) {
			val builder = EmbedBuilder()

			logger.debug { "Building footer page" }

			Page {
				color = DISCORD_BLURPLE
			}.build(
				localeObj,
				currentPageNum,
				chunkedPages,
				pages.groups[currentGroup]!!.size,
				groupEmoji,
				allGroups.indexOf(currentGroup),
				allGroups.size,
				shouldPutFooterInDescription = true,
				mutator = mutator?.pageMutator
			)(builder)

			if (!builder.description.isNullOrEmpty()) {
				if (this.embeds == null) {
					this.embeds = mutableListOf(builder)
				} else {
					this.embeds!!.add(builder)
				}
			}
		}

		mutator?.paginatorMutator?.let {
			it(this@BasePaginator)
		}
	}

	/** Send the paginator, given the current context. If it's already sent, update it. **/
	public abstract suspend fun send()

	/** Should be called as part of [send] in order to create buttons and get other things set up. **/
	public abstract suspend fun setup()

	/** Switch to the next group. Should not call [send]. **/
	public abstract suspend fun nextGroup()

	/** Switch to a specific page. Should not call [send]. **/
	public abstract suspend fun goToPage(page: Int)

	/** Destroy this paginator, removing its buttons and deleting its message if required.. **/
	public abstract suspend fun destroy()

	/** Convenience function to go to call [goToPage] with the next page number, if we're not at the last page. **/
	public open suspend fun nextPage() {
		if (currentPageNum < pages.groups[currentGroup]!!.size - 1) {
			goToPage(currentPageNum + chunkedPages)
		}
	}

	/** Convenience function to go to call [goToPage] with the previous page number, if we're not at the first page. **/
	public open suspend fun previousPage() {
		if (currentPageNum >= chunkedPages - 1) {
			goToPage(currentPageNum - chunkedPages)
		}
	}

	/**
	 * Register a callback that is called after the paginator times out.
	 *
	 * If there is no [timeoutSeconds] value set, your callbacks will never be called!
	 */
	public open fun onTimeout(body: suspend () -> Unit): BasePaginator {
		timeoutCallbacks.add(body)

		return this
	}

	/** @suppress Call the timeout callbacks. **/
	@Suppress("TooGenericExceptionCaught")  // Come on, now.
	public open suspend fun runTimeoutCallbacks() {
		timeoutCallbacks.forEach {
			try {
				it.invoke()
			} catch (t: Throwable) {
				logger.error(t) { "Error thrown by timeout callback: $it" }
			}
		}
	}
}
