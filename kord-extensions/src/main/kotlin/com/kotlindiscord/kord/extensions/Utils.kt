package com.kotlindiscord.kord.extensions

import com.gitlab.kordlib.core.Kord
import com.gitlab.kordlib.core.entity.Member
import com.gitlab.kordlib.core.entity.Message
import com.gitlab.kordlib.core.entity.Role
import com.gitlab.kordlib.core.event.Event
import com.gitlab.kordlib.core.firstOrNull
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.withTimeoutOrNull
import org.apache.commons.text.StringTokenizer

/**
 * Takes a [Message] object and parses it using a [StringTokenizer].
 *
 * This tokenizes a string, splitting it into an array of strings using whitespace as a
 * delimiter, but supporting quoted tokens (strings between quotes are treated as individual
 * arguments).
 *
 * This is used to create an array of arguments for a command's input.
 *
 * @param message The message to parse
 * @return An array of parsed arguments
 */
fun parseMessage(message: Message): Array<String> = StringTokenizer(message.content).tokenArray

/**
 * Convenience function to retrieve a user's top [Role].
 *
 * @receiver The [Member] to get the top role for.
 * @return The user's top role, or `null` if they have no roles.
 */
suspend fun Member.getTopRole(): Role? = this.roles.toList().max()

/**
 * Return the first received event that match the condition.
 *
 * @param T Event to wait for.
 * @param timeout Time before returning null, if no match can be done. Set to null to disable it.
 * @param condition Function return true if the event object is valid and should be returned.
 */
@Suppress("ExpressionBodySyntax")
suspend inline fun <reified T : Event> Kord.waitFor(
    timeout: Long? = null,
    noinline condition: (suspend T.() -> Boolean) = { true }
): T? {
    return if (timeout == null) {
        events.filterIsInstance<T>().firstOrNull(condition)
    } else {
        withTimeoutOrNull(timeout) {
            events.filterIsInstance<T>().firstOrNull(condition)
        }
    }
}
