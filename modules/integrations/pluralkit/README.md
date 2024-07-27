# PluralKit Extension

[![Discord: Click here](https://img.shields.io/static/v1?label=Discord&message=Click%20here&color=7289DA&style=for-the-badge&logo=discord)](https://discord.gg/ZKRetPNtvY)

The user-agent used for all PK requests is `Kord Extensions, extra-pluralkit`

---

This module contains an extension intended to ease the development of bots that wish to support
[PluralKit](https://pluralkit.me/) users. PluralKit is a Discord bot that attempts to make Discord more comfortable
to use for [plural systems](https://morethanone.info),
and other folks that might benefit from using it as an accessibility tool.

This extension is partially a development tool, and you can't install it as a plugin.
However, it provides guild staff members with several configuration commands, for guild-specific configuration
requirements.

**Note:** As Discord recommends that all bots use slash commands, this module doesn't provide any chat commands.

# Setting Up

## Gradle Plugin

With the [KordEx Gradle plugin](https://docs.kordex.dev/kordex-plugin.html) applied, add the module to your project:

```kt
kordEx {
	module("pluralkit")
}
```

## Manual Setup

* **Maven repo:** https://snapshots-repo.kordex.dev
* **Maven coordinates:** `dev.kordex.modules:pluralkit:VERSION`

To manually add the module to your project, follow these steps:

1. Add the module to your project.
	- `dev.kordex.modules:pluralkit:VERSION`

# Usage

At its simplest, you can add this extension directly to your bot with the default configuration. For example:

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

This will install the extension using its default configuration.
However, you may configure the extension in several ways, explained below.

**Note:** The *Manage Webhooks* permission is no longer required!

# Commands

This extension provides the following commands for use on Discord.

* Slash command: `/pluralkit`, for per-guild settings.

# Configuration

The PluralKit extension has two forms of configuration – global and per-guild.

## Global Configuration

To avoid hammering a given PK-compatible API, you may configure the PluralKit extension's rate limiting.

```kt
extensions {
	extPluralKit {
		defaultLimit(4, 1.seconds)

		domainLimit("api.pluralkit.me", 2, 1.seconds)
	}
}
```

You may configure rate limits using the following functions:

- `defaultLimit(limit, interval)` - Set the default limit to `limit` requests per `interval`.
- `unlimitByDefault()` - Remove the default rate limiter, disabling rate limiting for all domains without configured
  rate limits.


- `domainLimit(domain, limit, interval)` - Configure a separate rate limit for a specific domain.
- `defaultDomainLimit(domain)` - Remove a configured domain-specific rate limit, making it use the default rate limit.
- `unlimitDomain(domain)` - Disable rate limiting for the given domain, even when you provide a global rate limit.

The default global rate limit is **two requests per second**.

The extension also provides the following domain-specific rate limits:

- PluralKit API (`api.pluralkit.me`) - [Two requests per second.](https://pluralkit.me/api/#rate-limiting)

## Guild Configuration

The `/pluralkit` command allows for per-guild configuration.
Guild staff members must have the **Manage Server** permission to use this command.

- `api-url`: If you're using a fork of PluralKit or running your own instance, you can provide its API base URL here.
  Don't include the version in the URL - `https://api.pluralkit.me` is the default base URL, and doesn't
  include the `/v2` at the end.
- `bot`: If you're using a fork of PluralKit or running your own instance, you can provide the bot's use ID here.
  The extension will not handle proxying for messages proxied by other bots.
- `toggle-support`: You can use this option to forcibly disable PK support on the current server, if needed.

# Dev Usage

This extension simply fires the following events as required:

* `PKMessageCreateEvent`
* `PKMessageDeleteEvent`
* `PKMessageUpdateEvent`

These events contain extra information about whom sent the message, and the message it was in reply to.
If you need to get PK-specific information (or only match proxied or un-proxied messages),
you may use the event subtypes, prefixed with `Proxied` and `Unproxied` respectively.

# Notes

As the PluralKit API is a little brittle at times, and it is impossible to write this kind of logic without introducing
race conditions, you may find things don't always work as expected.

We're happy to accept suggestions on further refining the extension and rate limiting system.
However, without a reactive API (such as a WebSocket) on PluralKit's end, it is likely impossible to make the
extension behave perfectly.

We've made some suggestions to the PluralKit team, but it is worth remembering that PK is a volunteer-run project,
and a reactive API would be a complex addition.
