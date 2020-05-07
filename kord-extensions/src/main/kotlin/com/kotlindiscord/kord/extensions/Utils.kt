package com.kotlindiscord.kord.extensions

import com.gitlab.kordlib.core.entity.Message
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
fun parseMessage(message: Message): Array<String> {
    val array = StringTokenizer(message.content).tokenArray
    return array.sliceArray(1 until array.size)
}
