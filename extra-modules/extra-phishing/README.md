# Phishing Extension

[![Discord: Click here](https://img.shields.io/static/v1?label=Discord&message=Click%20here&color=7289DA&style=for-the-badge&logo=discord)](https://discord.gg/ZKRetPNtvY) <br />
![Latest](https://img.shields.io/maven-metadata/v?label=Latest&metadataUrl=https%3A%2F%2Fs01.oss.sonatype.org%2Fservice%2Flocal%2Frepositories%2Fsnapshots%2Fcontent%2Fcom%2Fkotlindiscord%2Fkord%2Fextensions%2Fkord-extensions%2Fmaven-metadata.xml&style=for-the-badge)

This module contains an extension written to provide some anti-phishing protection, based on the
crowdsourced [Sinking Yachts API](https://phish.sinking.yachts/docs).

# Getting Started

* **Maven repo:** Maven Central for releases, `https://s01.oss.sonatype.org/content/repositories/snapshots` for
  snapshots
* **Maven coordinates:** `com.kotlindiscord.kord.extensions:extra-phishing:VERSION`

At its simplest, you can add this extension directly to your bot with a minimum configuration. For example:

```kotlin
suspend fun main() {
    val bot = ExtensibleBot(System.getenv("TOKEN")) {

        extensions {
            extPhishing {}
        }
    }

    bot.start()
}
```

This will install the extension using its default configuration. However, the extension may be configured in several
ways - as is detailed below.

# Commands

This extension provides a number of commands for use on Discord.

* Slash command: `/phishing-check`, for checking whether the given argument is a phishing domain
* Message command: `Phishing Check`, for manually checking a specific message via the right-click menu

Access to both commands can be limited to a specific Discord permission. This can be configured below, but defaults to "
Manage Messages".

# Configuration

To configure this module, values can be provided within the `extPhishing` builder.

* `detectionAction` (default: Delete) - What to do when a message containing a phishing domain is detected
* `logChannelName` (default: "logs") - The name of the channel to use for logging; the extension will search the
  channels present on the current server and use the last one with an exactly-matching name
* `notifyUser` (default: True) - Whether to DM the user, letting them know they posted a phishing domain and what action
  was taken
* `requiredCommandPermission` (default: Manage Server) - The permission a user must have in order to run the bundled
  message and slash commands
* `updateDelay` (default: 15 minutes) - How often to check for new phishing domains, five minutes at minimum
* `urlRegex` (default: [The Perfect URL Regex](https://urlregex.com/)) - A regular expression used to extract domains
  from messages, with **exactly one capturing group containing the entire domain** (and optionally the URL path)

Additionally, the following configuration functions are available:

* `check` - Used to define checks that must pass for event handlers to be run, and thus for messages to be checked (you
  could use this to exempt your staff, for example)
* `regex` - A convenience function for registering a `String` as URL regex, with the case-insensitive flag
