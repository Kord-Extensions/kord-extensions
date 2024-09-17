/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.modules.func.phishing

import dev.kordex.core.i18n.types.Key
import dev.kordex.modules.func.phishing.i18n.generated.PhishingTranslations

/**
 * Sealed class representing what should happen when a phishing link is detected.
 *
 * The extension will always try to log the message, but you can specify [LogOnly] if that's _all_ you want.
 *
 * @property message Message to return to the user.
 */
sealed class DetectionAction(val message: Key) {
	/** Ban 'em and delete the message. **/
	object Ban : DetectionAction(PhishingTranslations.Actions.Ban.text)

	/** Delete the message. **/
	object Delete : DetectionAction(PhishingTranslations.Actions.Delete.text)

	/** Kick 'em and delete the message. **/
	object Kick : DetectionAction(PhishingTranslations.Actions.Kick.text)

	/** Don't do anything, just log it in the logs channel. **/
	object LogOnly : DetectionAction(PhishingTranslations.Actions.LogOnly.text)
}
