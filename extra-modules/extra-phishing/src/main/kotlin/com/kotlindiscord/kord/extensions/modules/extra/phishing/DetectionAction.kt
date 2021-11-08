package com.kotlindiscord.kord.extensions.modules.extra.phishing

/**
 * Sealed class representing what should happen when a phishing link is detected.
 *
 * The extension will always try to log the message, but you can specify [LogOnly] if that's _all_ you want.
 *
 * @property message Message to return to the user.
 */
sealed class DetectionAction(val message: String) {
    /** Ban 'em and delete the message. **/
    object Ban : DetectionAction("you have been banned from the server")

    /** Delete the message. **/
    object Delete : DetectionAction("it has been deleted")

    /** Kick 'em and delete the message. **/
    object Kick : DetectionAction("you have been kicked from the server")

    /** Don't do anything, just log it in the logs channel. **/
    object LogOnly : DetectionAction("it has been logged for the server staff to review")
}
