@file:OptIn(KordPreview::class)

package com.kotlindiscord.kord.extensions.pagination

import com.kotlindiscord.kord.extensions.components.ComponentCheckFun
import com.kotlindiscord.kord.extensions.components.Components
import com.kotlindiscord.kord.extensions.components.builders.DisabledButtonBuilder
import com.kotlindiscord.kord.extensions.components.builders.InteractiveButtonBuilder
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.pagination.pages.Pages
import com.kotlindiscord.kord.extensions.utils.capitalizeWords
import dev.kord.common.annotation.KordPreview
import dev.kord.common.entity.ButtonStyle
import dev.kord.core.entity.ReactionEmoji
import dev.kord.core.entity.User
import java.util.*

/** Last row number. **/
public const val LAST_ROW: Int = 4

/** Second row number. **/
public const val SECOND_ROW: Int = 1

/**
 * Abstract class containing some common functionality needed by interactive button-based paginators.
 */
public abstract class BaseButtonPaginator(
    extension: Extension,
    pages: Pages,
    owner: User? = null,
    timeoutSeconds: Long? = null,
    keepEmbed: Boolean = true,
    switchEmoji: ReactionEmoji = if (pages.groups.size == 2) EXPAND_EMOJI else SWITCH_EMOJI,
    bundle: String? = null,
    locale: Locale? = null,
) : BasePaginator(extension, pages, owner, timeoutSeconds, keepEmbed, switchEmoji, bundle, locale) {
    /** [Components] instance managing the buttons for this paginator. **/
    public abstract var components: Components

    /** Button builder representing the button that switches to the first page. **/
    public open var firstPageButton: InteractiveButtonBuilder? = null

    /** Button builder representing the button that switches to the previous page. **/
    public open var backButton: InteractiveButtonBuilder? = null

    /** Button builder representing the button that switches to the next page. **/
    public open var nextButton: InteractiveButtonBuilder? = null

    /** Button builder representing the button that switches to the last page. **/
    public open var lastPageButton: InteractiveButtonBuilder? = null

    /** Button builder representing the button that switches between groups. **/
    public open var switchButton: InteractiveButtonBuilder? = null

    /** Group-specific buttons, if any. **/
    public open val groupButtons: MutableMap<String, InteractiveButtonBuilder> = mutableMapOf()

    /** Whether it's possible for us to have a row of group-switching buttons. **/
    @Suppress("MagicNumber")
    public val canUseSwitchingButtons: Boolean = allGroups.size in 3..5 && "" !in allGroups

    /** A button-oriented check function that matches based on the [owner] property. **/
    public val defaultCheck: ComponentCheckFun = {
        if (owner == null) {
            true
        } else {
            it.interaction.user.id == owner.id
        }
    }

    override suspend fun setup() {
        components.onTimeout {
            destroy()
        }

        if (pages.size > 1) {
            // Add navigation buttons...
            firstPageButton = components.interactiveButton {
                deferredAck = true
                style = ButtonStyle.Secondary

                check(defaultCheck)
                check { active }

                emoji(FIRST_PAGE_EMOJI)

                action {
                    goToPage(0)

                    send()
                }
            }

            backButton = components.interactiveButton {
                deferredAck = true
                style = ButtonStyle.Secondary

                check(defaultCheck)
                check { active }

                emoji(LEFT_EMOJI)

                action {
                    previousPage()

                    send()
                }
            }

            nextButton = components.interactiveButton {
                deferredAck = true
                style = ButtonStyle.Secondary

                check(defaultCheck)
                check { active }

                emoji(RIGHT_EMOJI)

                action {
                    nextPage()

                    send()
                }
            }

            lastPageButton = components.interactiveButton {
                deferredAck = true
                style = ButtonStyle.Secondary

                check(defaultCheck)
                check { active }

                emoji(LAST_PAGE_EMOJI)

                action {
                    goToPage(pages.size - 1)

                    send()
                }
            }
        }

        if (pages.size > 1 || !keepEmbed) {
            // Add the destroy button
            components.interactiveButton(LAST_ROW) {
                deferredAck = true

                check(defaultCheck)
                check { active }

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
                    groupButtons[group] = components.interactiveButton(SECOND_ROW) {
                        deferredAck = true
                        label = translate(group).capitalizeWords(localeObj)
                        style = ButtonStyle.Secondary

                        check(defaultCheck)
                        check { active }

                        action {
                            switchGroup(group)
                        }
                    }
                }
            } else {
                // Add the singular switch button

                switchButton = components.interactiveButton(LAST_ROW) {
                    deferredAck = true

                    check(defaultCheck)
                    check { active }

                    emoji(switchEmoji)

                    label = if (allGroups.size == 2) {
                        translate("paginator.button.more")
                    } else {
                        translate("paginator.button.group.switch")
                    }

                    action {
                        nextGroup()

                        send()
                    }
                }
            }
        }

        components.sortIntoRows()
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
    public fun setDisabledButton(oldButton: InteractiveButtonBuilder?): Boolean {
        oldButton ?: return false

        val newButton = DisabledButtonBuilder()

        // Copy properties from the old button
        newButton.id = oldButton.id
        newButton.label = oldButton.label
        newButton.partialEmoji = oldButton.partialEmoji
        newButton.style = oldButton.style

        // Find the old button and replace it
        components.rows.forEach { row ->
            val index = row.indexOfFirst { it is InteractiveButtonBuilder && it.id == oldButton.id }

            if (index != -1) {
                row[index] = newButton

                return true
            }
        }

        return false
    }

    /** Replace a disabled button in [components] with the given interactive button of the same ID.. **/
    public fun setEnabledButton(newButton: InteractiveButtonBuilder?): Boolean {
        newButton ?: return false

        // Find the disabled button and replace it
        components.rows.forEach { row ->
            val index = row.indexOfFirst { it is DisabledButtonBuilder && it.id == newButton.id }

            if (index != -1) {
                row[index] = newButton

                return true
            }
        }

        return false
    }
}
