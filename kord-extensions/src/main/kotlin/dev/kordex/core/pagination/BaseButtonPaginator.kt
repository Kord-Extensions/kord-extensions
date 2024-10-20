/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.pagination

import dev.kord.common.entity.ButtonStyle
import dev.kord.core.behavior.UserBehavior
import dev.kord.core.entity.ReactionEmoji
import dev.kord.core.event.interaction.ComponentInteractionCreateEvent
import dev.kordex.core.checks.types.CheckWithCache
import dev.kordex.core.components.ComponentContainer
import dev.kordex.core.components.buttons.PublicInteractionButton
import dev.kordex.core.components.publicButton
import dev.kordex.core.components.types.emoji
import dev.kordex.core.i18n.EMPTY_KEY
import dev.kordex.core.i18n.capitalizeWords
import dev.kordex.core.i18n.generated.CoreTranslations
import dev.kordex.core.i18n.types.Key
import dev.kordex.core.pagination.builders.PageTransitionCallback
import dev.kordex.core.pagination.pages.Pages
import dev.kordex.core.utils.capitalizeWords
import dev.kordex.core.utils.scheduling.Scheduler
import dev.kordex.core.utils.scheduling.Task
import kotlinx.coroutines.runBlocking
import java.util.*
import kotlin.math.ceil
import kotlin.math.roundToInt

/**
 * Abstract class containing some common functionality needed by interactive button-based paginators.
 */
public abstract class BaseButtonPaginator(
	pages: Pages,
	chunkedPages: Int = 1,
	owner: UserBehavior? = null,
	timeoutSeconds: Long? = null,
	keepEmbed: Boolean = true,
	switchEmoji: ReactionEmoji = if (pages.groups.size == 2) EXPAND_EMOJI else SWITCH_EMOJI,
	mutator: PageTransitionCallback? = null,
	locale: Locale? = null,
) : BasePaginator(pages, chunkedPages, owner, timeoutSeconds, keepEmbed, switchEmoji, mutator, locale) {
	/** [ComponentContainer] instance managing the buttons for this paginator. **/
	public open var components: ComponentContainer = ComponentContainer()

	/** Scheduler used to schedule the paginator's timeout. **/
	public var scheduler: Scheduler = Scheduler()

	/** Scheduler used to schedule the paginator's timeout. **/
	public var task: Task? = if (timeoutSeconds != null) {
		runBlocking { // This is a trivially quick block, so it should be fine.
			scheduler.schedule(timeoutSeconds) { destroy() }
		}
	} else {
		null
	}

	private val lastRowNumber by lazy { components.rows.size - 1 }
	private val secondRowNumber = 1

	/** Button builder representing the button that switches to the first page. **/
	public open var firstPageButton: PublicInteractionButton<*>? = null

	/** Button builder representing the button that switches to the previous page. **/
	public open var backButton: PublicInteractionButton<*>? = null

	/** Button builder representing the button that switches to the next page. **/
	public open var nextButton: PublicInteractionButton<*>? = null

	/** Button builder representing the button that switches to the last page. **/
	public open var lastPageButton: PublicInteractionButton<*>? = null

	/** Button builder representing the button that switches between groups. **/
	public open var switchButton: PublicInteractionButton<*>? = null

	/** Group-specific buttons, if any. **/
	public open val groupButtons: MutableMap<Key, PublicInteractionButton<*>> = mutableMapOf()

	/** Whether it's possible for us to have a row of group-switching buttons. **/
	@Suppress("MagicNumber")
	public val canUseSwitchingButtons: Boolean by lazy { allGroups.size in 3..5 && EMPTY_KEY !in allGroups }

	/** A button-oriented check function that matches based on the [owner] property. **/
	public val defaultCheck: CheckWithCache<ComponentInteractionCreateEvent> = {
		if (!active) {
			fail()
		} else if (owner == null) {
			pass()
		} else if (event.interaction.user.id == owner.id) {
			pass()
		} else {
			fail()
		}
	}

	override suspend fun destroy() {
		runTimeoutCallbacks()
		task?.cancel()
	}

	@Suppress("MagicNumber")
	override suspend fun setup() {
		if (chunkedPages < 1) {
			error("You must have at least 1 chunked page per message..")
		}

		if (chunkedPages > 9) {
			error("You may only have up to 9 chunked pages per message.")
		}

		if (pages.groups.values.any { it.size > 1 }) {
			// Add navigation buttons...
			firstPageButton = components.publicButton {
				deferredAck = true
				style = ButtonStyle.Secondary
				disabled = pages.groups[currentGroup]!!.size <= 1

				check(defaultCheck)

				emoji(FIRST_PAGE_EMOJI)

				action {
					goToPage(0)

					send()
					task?.restart()
				}
			}

			backButton = components.publicButton {
				deferredAck = true
				style = ButtonStyle.Secondary
				disabled = pages.groups[currentGroup]!!.size <= 1

				check(defaultCheck)

				emoji(LEFT_EMOJI)

				action {
					previousPage()

					send()
					task?.restart()
				}
			}

			nextButton = components.publicButton {
				deferredAck = true
				style = ButtonStyle.Secondary
				disabled = pages.groups[currentGroup]!!.size <= chunkedPages

				check(defaultCheck)

				emoji(RIGHT_EMOJI)

				action {
					nextPage()

					send()
					task?.restart()
				}
			}

			lastPageButton = components.publicButton {
				deferredAck = true
				style = ButtonStyle.Secondary
				disabled = pages.groups[currentGroup]!!.size <= chunkedPages

				check(defaultCheck)

				emoji(LAST_PAGE_EMOJI)

				action {
					// This is a mess, but I'm not great at math.
					goToPage(
						ceil(
							pages.groups[currentGroup]!!.size.div(chunkedPages.toFloat())
						)
							.roundToInt()
							.times(chunkedPages)
							.minus(chunkedPages)
					)

					send()
					task?.restart()
				}
			}
		}

		if (pages.groups.values.any { it.size > 1 } || !keepEmbed) {
			// Add the destroy button
			components.publicButton(lastRowNumber) {
				deferredAck = true

				check(defaultCheck)

				label = if (keepEmbed) {
					style = ButtonStyle.Primary
					emoji(FINISH_EMOJI)

					CoreTranslations.Paginator.Button.done
						.withLocale(localeObj)
				} else {
					style = ButtonStyle.Danger
					emoji(DELETE_EMOJI)

					CoreTranslations.Paginator.Button.delete
						.withLocale(localeObj)
				}

				action {
					destroy()
				}
			}
		}

		if (pages.groups.size > 1) {
			if (canUseSwitchingButtons) {
				// Add group-switching buttons

				allGroups.forEach { group ->
					groupButtons[group] = components.publicButton(secondRowNumber) {
						deferredAck = true

						label = group
							.withLocale(localeObj)
							.capitalizeWords()

						style = ButtonStyle.Secondary

						check(defaultCheck)

						action {
							switchGroup(group)
							task?.restart()
						}
					}
				}
			} else {
				// Add the singular switch button

				switchButton = components.publicButton(lastRowNumber) {
					deferredAck = true

					check(defaultCheck)

					emoji(switchEmoji)

					label = if (allGroups.size == 2) {
						CoreTranslations.Paginator.Button.more
							.withLocale(localeObj)
					} else {
						CoreTranslations.Paginator.Button.Group.switch
							.withLocale(localeObj)
					}

					action {
						nextGroup()

						send()
						task?.restart()
					}
				}
			}
		}

		components.sort()
		updateButtons()
	}

	/**
	 * Convenience function to switch to a specific group.
	 */
	public suspend fun switchGroup(group: Key) {
		if (group == currentGroup) {
			return
		}

		// To avoid out-of-bounds
		currentPageNum = minOf(currentPageNum, pages.groups[group]!!.size)
		currentPages = getChunk()
		currentGroup = group

		send()
	}

	override suspend fun nextGroup() {
		val current = currentGroup
		val nextIndex = allGroups.indexOf(current) + 1

		if (nextIndex >= allGroups.size) {
			switchGroup(allGroups.first())
		} else {
			switchGroup(allGroups[nextIndex])
		}
	}

	override suspend fun goToPage(page: Int) {
		logger.debug { "Going to page: $page" }

		if (page == currentPageNum) {
			logger.debug { "Page number $page is the same as the current page!" }

			return
		}

		if (page < 0 || page > pages.groups[currentGroup]!!.size - 1) {
			logger.debug { "Page number $page is too high!" }

			return
		}

		currentPageNum = page
		currentPages = getChunk()

		send()
	}

	/**
	 * Convenience function that enables and disables buttons as necessary, depending on the current page number.
	 */
	public fun updateButtons() {
		if (currentPageNum <= 0) {
			setDisabledButton(firstPageButton)
			setDisabledButton(backButton)
		} else {
			setEnabledButton(firstPageButton)
			setEnabledButton(backButton)
		}

		if (currentPageNum + chunkedPages > pages.groups[currentGroup]!!.size - 1) {
			setDisabledButton(nextButton)
			setDisabledButton(lastPageButton)
		} else {
			setEnabledButton(nextButton)
			setEnabledButton(lastPageButton)
		}

		if (allGroups.size == 2) {
			if (currentGroup == pages.defaultGroup) {
				switchButton?.label = CoreTranslations.Paginator.Button.more
					.withLocale(localeObj)
			} else {
				switchButton?.label = CoreTranslations.Paginator.Button.less
					.withLocale(localeObj)
			}
		}

		if (canUseSwitchingButtons) {
			groupButtons.forEach { (key, value) ->
				if (key == currentGroup) {
					setDisabledButton(value)
				} else {
					setEnabledButton(value)
				}
			}
		}
	}

	/** Replace an enabled interactive button in [components] with a disabled button of the same ID. **/
	public fun setDisabledButton(button: PublicInteractionButton<*>?) {
		button ?: return

		button.disable()
	}

	/** Replace a disabled button in [components] with the given interactive button of the same ID. **/
	public fun setEnabledButton(button: PublicInteractionButton<*>?) {
		button ?: return

		button.enable()
	}
}
