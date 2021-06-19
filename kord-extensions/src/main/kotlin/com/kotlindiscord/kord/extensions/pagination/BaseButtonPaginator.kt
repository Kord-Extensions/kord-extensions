@file:OptIn(KordPreview::class)

package com.kotlindiscord.kord.extensions.pagination

import com.kotlindiscord.kord.extensions.components.ButtonCheckFun
import com.kotlindiscord.kord.extensions.components.Components
import com.kotlindiscord.kord.extensions.components.builders.DisabledButtonBuilder
import com.kotlindiscord.kord.extensions.components.builders.InteractiveButtonBuilder
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.pagination.pages.Pages
import dev.kord.common.annotation.KordPreview
import dev.kord.core.entity.ReactionEmoji
import dev.kord.core.entity.User
import java.util.*

/** Last row number. **/
public const val LAST_ROW: Int = 4

/**
 * Abstract class containing some common functionality needed by interactive button-based paginators.
 */
public abstract class BaseButtonPaginator(
    extension: Extension,
    pages: Pages,
    owner: User? = null,
    timeoutSeconds: Long? = null,
    keepEmbed: Boolean = true,
    switchEmoji: ReactionEmoji.Unicode = if (pages.groups.size == 2) EXPAND_EMOJI else SWITCH_EMOJI,
    locale: Locale? = null,
) : BasePaginator(extension, pages, owner, timeoutSeconds, keepEmbed, switchEmoji, locale) {
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

    /** A button-oriented check function that matches based on the [owner] property. **/
    public val defaultCheck: ButtonCheckFun = {
        if (owner == null) {
            true
        } else {
            val user = it.interaction.user

            user.id == owner.id
        }
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
