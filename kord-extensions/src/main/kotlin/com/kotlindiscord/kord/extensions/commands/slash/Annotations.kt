@file:Suppress("Filename", "MatchingDeclarationName")

package com.kotlindiscord.kord.extensions.commands.slash

@RequiresOptIn(
    message = "Due to limitations in the Discord API, it's not currently possible to translate this property.",

    level = RequiresOptIn.Level.WARNING
)
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.PROPERTY)
/** Opt-in annotation to alert users that a string can't be translated yet. **/
public annotation class TranslationNotSupported
