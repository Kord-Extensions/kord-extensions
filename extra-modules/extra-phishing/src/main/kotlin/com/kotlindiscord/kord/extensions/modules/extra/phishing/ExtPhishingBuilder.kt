/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

@file:OptIn(ExperimentalTime::class)
@file:Suppress("MagicNumber")

package com.kotlindiscord.kord.extensions.modules.extra.phishing

import com.kotlindiscord.kord.extensions.checks.types.Check
import dev.kord.common.entity.Permission
import dev.kord.core.event.Event
import kotlin.time.Duration.Companion.minutes
import kotlin.time.ExperimentalTime

/** Builder used to configure the phishing extension. **/
class ExtPhishingBuilder {
    /** The name of your application, which allows the Sinking Yachts maintainers to identify what it is. **/
    lateinit var appName: String

    /** Delay between domain update checks, 5 minutes at minimum. **/
    var updateDelay = 15.minutes

    /**
     * Regular expression used to extract domains from messages.
     *
     * If you customize this, it must contain exactly one capturing group, containing the full domain name, and
     * optionally the path, if this is an actual URL. You can mark a group as non-capturing by prefixing it with
     * `?:`.
     *
     * The provided regex comes from https://urlregex.com/ - but you can provide a different regex if you need
     * detection to be more sensitive than just clickable links.
     */
    var urlRegex = "(?:https?|ftp|file|discord)://([-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|])"
        .toRegex(RegexOption.IGNORE_CASE)

    /** @suppress List of checks to apply to event handlers. **/
    val checks: MutableList<Check<Event>> = mutableListOf()

    /**
     * If you want to require a permission for the phishing check commands, supply it here. Alternatively, supply
     * `null` and everyone will be given access to them.
     */
    var requiredCommandPermission: Permission? = Permission.ManageMessages

    /**
     * What to do when a message creation/edit contains a phishing domain.
     *
     * @see DetectionAction
     */
    var detectionAction: DetectionAction = DetectionAction.Delete

    /** Whether to DM users when their messages contain phishing domains, with the action taken. **/
    var notifyUser = true

    /**
     * The name of the logs channel to use for detection messages, if not "logs".
     *
     * The extension will try to find the last channel in the channel list with a name exactly matching the
     * given name here, "logs" by default.
     */
    var logChannelName = "logs"

    /** Register a check that must pass in order for an event handler to run, and for messages to be processed. **/
    fun check(check: Check<Event>) {
        checks.add(check)
    }

    /** Register checks that must pass in order for an event handler to run, and for messages to be processed. **/
    fun check(vararg checkList: Check<Event>) {
        checks.addAll(checkList)
    }

    /** Convenience function for supplying a case-insensitive [urlRegex]. **/
    fun regex(pattern: String) {
        urlRegex = pattern.toRegex(RegexOption.IGNORE_CASE)
    }

    /** @suppress **/
    fun validate() {
        if (!this::appName.isInitialized) {
            error("Application name must be provided")
        }

        if (updateDelay < 5.minutes) {
            error("The update delay must be at least five minutes - don't spam the API!")
        }
    }
}
