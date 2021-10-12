package com.kotlindiscord.kord.extensions.pagination

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.i18n.TranslationsProvider
import com.kotlindiscord.kord.extensions.pagination.pages.Page
import com.kotlindiscord.kord.extensions.pagination.pages.Pages
import dev.kord.core.Kord
import dev.kord.core.behavior.UserBehavior
import dev.kord.core.entity.ReactionEmoji
import dev.kord.rest.builder.message.EmbedBuilder
import mu.KotlinLogging
import org.koin.core.component.KoinComponent
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
 * @param owner Optional paginator owner - setting this will prevent other users from interacting with the paginator
 * @param timeoutSeconds How long (in seconds) to wait before destroying the paginator, if needed
 * @param keepEmbed Set this to `false` to remove the paginator's message when it's destroyed
 * @param switchEmoji The `ReactionEmoji` to use for group switching
 * @param locale A Locale object for this pagination context, which defaults to the bot's default locale
 * @param bundle Translation bundle to use for this paginator
 */
public abstract class BasePaginator(
    public open val pages: Pages,
    public open val owner: UserBehavior? = null,
    public open val timeoutSeconds: Long? = null,
    public open val keepEmbed: Boolean = true,
    public open val switchEmoji: ReactionEmoji = if (pages.groups.size == 2) EXPAND_EMOJI else SWITCH_EMOJI,
    public open val bundle: String? = null,

    locale: Locale? = null
) : KoinComponent {
    private val logger = KotlinLogging.logger {}

    /** Current instance of the bot. **/
    public val bot: ExtensibleBot by inject()

    /** Kord instance, backing the ExtensibleBot. **/
    public val kord: Kord by inject()

    /** Current translations provider. **/
    public val translations: TranslationsProvider by inject()

    /** Locale to use for translations. **/
    public open val localeObj: Locale = locale ?: bot.settings.i18nBuilder.defaultLocale

    /** What to do after the paginator times out. **/
    public val timeoutCallbacks: MutableList<suspend () -> Unit> = mutableListOf()

    /** Currently-displayed page index. **/
    public var currentPageNum: Int = 0

    /** Currently-displayed page group. **/
    public var currentGroup: String = pages.defaultGroup

    /** Whether this paginator is currently active and processing events. **/
    public open var active: Boolean = true

    /** Set of all page groups. **/
    public open var allGroups: List<String> = pages.groups.map { it.key }

    /** Currently-displayed page object. **/
    public open var currentPage: Page = pages.get(currentGroup, currentPageNum)

    /** Builder that generates an embed for the paginator's current context. **/
    public open suspend fun EmbedBuilder.applyPage() {
        val groupEmoji = if (pages.groups.size > 1) {
            currentGroup
        } else {
            null
        }

        currentPage.build(
            localeObj,
            currentPageNum,
            pages.size,
            groupEmoji,
            allGroups.indexOf(currentGroup),
            allGroups.size
        )()
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
        if (currentPageNum < pages.size - 1) {
            goToPage(currentPageNum + 1)
        }
    }

    /** Convenience function to go to call [goToPage] with the previous page number, if we're not at the first page. **/
    public open suspend fun previousPage() {
        if (currentPageNum != 0) {
            goToPage(currentPageNum - 1)
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

    /** Quick access to translations, using the paginator's locale and bundle. **/
    public fun translate(key: String, replacements: Array<Any?> = arrayOf()): String =
        translations.translate(key, localeObj, bundle, replacements = replacements)
}
