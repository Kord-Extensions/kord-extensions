/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

@file:OptIn(ExperimentalTime::class)
@file:Suppress("MagicNumber")

package dev.kordex.modules.func.phishing

import dev.kord.common.entity.Permission
import dev.kord.core.event.Event
import dev.kordex.core.checks.types.CheckWithCache
import kotlin.time.Duration.Companion.minutes
import kotlin.time.ExperimentalTime

/** Builder used to configure the phishing extension. **/
class ExtPhishingBuilder {
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
	var urlRegex = "(?:https?|ftp|file|discord)://([-a-zA-Z\\d+&@#/%?=~_|!:,.;]*[-a-zA-Z\\d+&@#/%=~_|])"
		.toRegex(RegexOption.IGNORE_CASE)

	/** @suppress List of checks to apply to event handlers. **/
	val checks: MutableList<CheckWithCache<Event>> = mutableListOf()

	/**
	 * Set of bad domains, used as well as the usual unsafe domains.
	 *
	 * Contains a small list of known unsafe domains.
	 */
	val badDomains: MutableSet<String> = mutableSetOf(
		// Data broker for scraped Discord user and message data.
		// TODO: Switch to new domain when it exists
		// "spy.pet",
	)

	/**
	 * If you want to require a permission for the check commands, supply it here.
	 * Alternatively, supply `null` and everyone will be given access to them.
	 */
	var requiredCommandPermission: Permission? = Permission.ManageMessages

	/**
	 * What to do when a message creation/edit contains an unsafe domain.
	 *
	 * @see DetectionAction
	 */
	var detectionAction: DetectionAction = DetectionAction.Delete

	/** Whether to DM users when their messages contain unsafe domains, with the action taken. **/
	var notifyUser = true

	/**
	 * The name of the logs channel to use for detection messages, if not "logs".
	 *
	 * The extension will try to find the last channel in the channel list with a name exactly matching the
	 * given name here, "logs" by default.
	 */
	var logChannelName = "logs"

	/**
	 * Register a bad domain.
	 *
	 * The extension will treat them like other unsafe domains, removing messages linking to them.
	 */
	fun badDomain(domain: String) {
		badDomains.add(domain)
	}

	/** Register a check, which must pass in order for an event handler to run, and for messages to be processed. **/
	fun check(check: CheckWithCache<Event>) {
		checks.add(check)
	}

	/** Register checks, which must pass in order for an event handler to run, and for messages to be processed. **/
	fun check(vararg checkList: CheckWithCache<Event>) {
		checks.addAll(checkList)
	}

	/** Convenience function for supplying a case-insensitive [urlRegex]. **/
	fun regex(pattern: String) {
		urlRegex = pattern.toRegex(RegexOption.IGNORE_CASE)
	}

	/** @suppress **/
	fun validate() {
		if (updateDelay < 5.minutes) {
			error("The update delay must be at least five minutes - don't spam the API!")
		}
	}
}
