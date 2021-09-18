@file:OptIn(KordPreview::class)

package com.kotlindiscord.kord.extensions.pagination

import com.kotlindiscord.kord.extensions.checks.types.Check
import com.kotlindiscord.kord.extensions.components.ComponentContainer
import com.kotlindiscord.kord.extensions.components.buttons.DisabledInteractionButton
import com.kotlindiscord.kord.extensions.components.buttons.PublicInteractionButton
import com.kotlindiscord.kord.extensions.components.publicButton
import com.kotlindiscord.kord.extensions.components.types.emoji
import com.kotlindiscord.kord.extensions.pagination.pages.Pages
import com.kotlindiscord.kord.extensions.utils.capitalizeWords
import com.kotlindiscord.kord.extensions.utils.scheduling.Scheduler
import com.kotlindiscord.kord.extensions.utils.scheduling.Task
import dev.kord.common.annotation.KordPreview
import dev.kord.common.entity.ButtonStyle
import dev.kord.core.behavior.UserBehavior
import dev.kord.core.entity.ReactionEmoji
import dev.kord.core.event.interaction.ComponentInteractionCreateEvent
import java.util.*

/**
 * Abstract class containing some common functionality needed by interactive button-based paginators.
 */
public abstract class BaseButtonPaginator(
    pages: Pages,
    owner: UserBehavior? = null,
    timeoutSeconds: Long? = null,
    keepEmbed: Boolean = true,
    switchEmoji: ReactionEmoji = if (pages.groups.size == 2) EXPAND_EMOJI else SWITCH_EMOJI,
    bundle: String? = null,
    locale: Locale? = null,
) : BasePaginator(pages, owner, timeoutSeconds, keepEmbed, switchEmoji, bundle, locale) {
    /** [ComponentContainer] instance managing the buttons for this paginator. **/
    public open var components: ComponentContainer = ComponentContainer()

    /** Scheduler used to schedule the paginator's timeout. **/
    public var scheduler: Scheduler = Scheduler()

    /** Scheduler used to schedule the paginator's timeout. **/
    public var task: Task? = if (timeoutSeconds != null) {
        scheduler.schedule(timeoutSeconds) { destroy() }
    } else {
        null
    }

    private val lastRowNumber by lazy { components.rows.size - 1 }
    private val secondRowNumber = 1

    /** Button builder representing the button that switches to the first page. **/
    public open var firstPageButton: PublicInteractionButton? = null

    /** Button builder representing the button that switches to the previous page. **/
    public open var backButton: PublicInteractionButton? = null

    /** Button builder representing the button that switches to the next page. **/
    public open var nextButton: PublicInteractionButton? = null

    /** Button builder representing the button that switches to the last page. **/
    public open var lastPageButton: PublicInteractionButton? = null

    /** Button builder representing the button that switches between groups. **/
    public open var switchButton: PublicInteractionButton? = null

    /** Group-specific buttons, if any. **/
    public open val groupButtons: MutableMap<String, PublicInteractionButton> = mutableMapOf()

    /** Whether it's possible for us to have a row of group-switching buttons. **/
    @Suppress("MagicNumber")
    public val canUseSwitchingButtons: Boolean by lazy { allGroups.size in 3..5 && "" !in allGroups }

    /** A button-oriented check function that matches based on the [owner] property. **/
    public val defaultCheck: Check<ComponentInteractionCreateEvent> = {
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

    override suspend fun setup() {
        if (pages.size > 1) {
            // Add navigation buttons...
            firstPageButton = components.publicButton {
                deferredAck = true
                style = ButtonStyle.Secondary

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

                check(defaultCheck)

                emoji(LAST_PAGE_EMOJI)

                action {
                    goToPage(pages.size - 1)

                    send()
                    task?.restart()
                }
            }
        }

        if (pages.size > 1 || !keepEmbed) {
            // Add the destroy button
            components.publicButton(lastRowNumber) {
                deferredAck = true

                check(defaultCheck)

                label = if (keepEmbed) {
                    style = ButtonStyle.Primary
                    emoji(FINISH_EMOJI)

                    translate("paginator.button.done")
                } else {
                    style = ButtonStyle.Danger
                    emoji(DELETE_EMOJI)

                    translate("paginator.button.delete")
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
                        label = translate(group).capitalizeWords(localeObj)
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
                        translate("paginator.button.more")
                    } else {
                        translate("paginator.button.group.switch")
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
    public suspend fun switchGroup(group: String) {
        if (group == currentGroup) {
            return
        }

        currentPage = pages.get(group, currentPageNum)
        currentGroup = group

        send()
    }

    override suspend fun nextGroup() {
        val current = currentGroup
        val nextIndex = allGroups.indexOf(current) + 1

        currentGroup = if (nextIndex >= allGroups.size) {
            allGroups.first()
        } else {
            allGroups[nextIndex]
        }

        currentPage = pages.get(currentGroup, currentPageNum)

        send()
    }

    override suspend fun goToPage(page: Int) {
        if (page == currentPageNum) {
            return
        }

        if (page < 0 || page > pages.size - 1) {
            return
        }

        currentPageNum = page
        currentPage = pages.get(currentGroup, currentPageNum)

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

        if (currentPageNum >= pages.size - 1) {
            setDisabledButton(nextButton)
            setDisabledButton(lastPageButton)
        } else {
            setEnabledButton(nextButton)
            setEnabledButton(lastPageButton)
        }

        if (allGroups.size == 2) {
            if (currentGroup == pages.defaultGroup) {
                switchButton?.label = translate("paginator.button.more")
            } else {
                switchButton?.label = translate("paginator.button.less")
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
    public fun setDisabledButton(oldButton: PublicInteractionButton?): Boolean {
        oldButton ?: return false

        val newButton = DisabledInteractionButton()

        // Copy properties from the old button
        newButton.id = oldButton.id
        newButton.label = oldButton.label
        newButton.partialEmoji = oldButton.partialEmoji
        newButton.style = oldButton.style

        return components.replace(oldButton, newButton)
    }

    /** Replace a disabled button in [components] with the given interactive button of the same ID.. **/
    public fun setEnabledButton(newButton: PublicInteractionButton?): Boolean {
        newButton ?: return false

        return components.replace(newButton.id, newButton)
    }
}
