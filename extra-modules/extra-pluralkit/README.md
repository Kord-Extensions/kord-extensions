# Phishing Extension

[![Discord: Click here](https://img.shields.io/static/v1?label=Discord&message=Click%20here&color=7289DA&style=for-the-badge&logo=discord)](https://discord.gg/ZKRetPNtvY) <br />
![Latest](https://img.shields.io/maven-metadata/v?label=Latest&metadataUrl=https%3A%2F%2Fs01.oss.sonatype.org%2Fservice%2Flocal%2Frepositories%2Fsnapshots%2Fcontent%2Fcom%2Fkotlindiscord%2Fkord%2Fextensions%2Fkord-extensions%2Fmaven-metadata.xml&style=for-the-badge)

This module contains an extension intended to ease the development of bots that wish to support 
[PluralKit](https://pluralkit.me/) users. PluralKit is a Discord bot that attempts to make Discord more comfortable
to use for [plural systems](https://morethanone.info), and other people that may benefit from using it as a mental
health aid.

This extension is intended as a development aid, and cannot be installed as a plugin. However, individual guilds
may wish to configure it in some ways, so it provides a command with a few options.

**Note:** As Discord recommends that all bots make use of slash commands, this module does not provide extra
handling for chat commands.

# Getting Started

* **Maven repo:** Maven Central for releases, `https://oss.sonatype.org/content/repositories/snapshots/` for snapshots
* **Maven coordinates:** `com.kotlindiscord.kord.extensions:extra-pluralkit:VERSION`

At its simplest, you can add this extension directly to your bot with a minimum configuration. For example:

```kotlin
suspend fun main() {
    val bot = ExtensibleBot(System.getenv("TOKEN")) {

        extensions {
            extPluralKit()
        }
    }

    bot.start()
}
```

This will install the extension using its default configuration. However, the extension may be configured in several 
ways - as is detailed below.

Your bot will now require the **Manage Webhooks** permission to use the extension.

# Commands

This extension provides the following commands for use on Discord.

* Slash command: `/pluralkit`, for per-guild configuration of this extension, if needed.

# Configuration

The `/pluralkit` command allows you to configure settings on a per-guild basis. The following options can only be
changed or retrieved if you have the **Manage Server** permission on the current guild.

* `api-url`: If you're using a fork of PluralKit or running your own instance, you can provide its API base URL here.
  This should not include the version in the URL - `https://api.pluralkit.me` is the default base URL, and does not
  include the `/v2` at the end.
* `bot`: If you're using a fork of PluralKit or running your own instance, you can provide the bot's account here. The 
  extension will not handle proxying for messages proxied by other bots.
* `toggle-support`: You can use this option to forcibly disable PK support on the current server, if needed.

# Dev Usage

This extension simply fires the following events as required:

* `PKMessageCreateEvent`
* `PKMessageDeleteEvent`
* `PKMessageUpdateEvent`

These events contain extra information about who sent the message and which message was being replied to. If you need
to get PK-specific information (or only match proxied/unproxied messages), you can make use of the event subtypes,
which are prefixed with `Proxied` and `Unproxied` respectively.

As the PluralKit API is a little brittle at times, and it's impossible to write this kind of logic without introducing
race conditions, you may find occasions where things don't quite work as expected. While we'll accept suggestions on
further refining the system and better timing options, there will be no way to completely fix things unless the
PluralKit API introduces a reactive model (for example, a WebSocket) for their API -- something that is rather
unlikely due to the complexity involved.
