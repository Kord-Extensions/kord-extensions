# Kord Extensions

[![Docs: Click here](https://img.shields.io/static/v1?label=Docs&message=Click%20here&color=7289DA&style=for-the-badge&logo=read-the-docs)](https://kordex.kotlindiscord.com/) [![Discord: Click here](https://img.shields.io/static/v1?label=Discord&message=Click%20here&color=7289DA&style=for-the-badge&logo=discord)](https://discord.gg/ZKRetPNtvY) <br /> 
[![Build Status](https://img.shields.io/github/workflow/status/Kotlin-Discord/kord-extensions/CI/root?logo=github&style=for-the-badge)](https://github.com/Kotlin-Discord/kord-extensions/actions?query=workflow%3ACI+branch%3Aroot) [![Weblate project translated](https://img.shields.io/weblate/progress/kord-extensions?style=for-the-badge)]((https://hosted.weblate.org/engage/kord-extensions/)) <br />
[![Release](https://img.shields.io/nexus/r/com.kotlindiscord.kord.extensions/kord-extensions?nexusVersion=3&logo=gradle&color=blue&label=Release&server=https%3A%2F%2Fmaven.kotlindiscord.com&style=for-the-badge)](https://maven.kotlindiscord.com/#browse/browse:maven-releases:com%2Fkotlindiscord%2Fkord%2Fextensions%2Fkord-extensions) [![Snapshot](https://img.shields.io/nexus/s/com.kotlindiscord.kord.extensions/kord-extensions?logo=gradle&color=orange&label=Snapshot&server=https%3A%2F%2Fmaven.kotlindiscord.com&style=for-the-badge)](https://maven.kotlindiscord.com/#browse/browse:maven-snapshots:com%2Fkotlindiscord%2Fkord%2Fextensions%2Fkord-extensions)

[![Translation status](https://hosted.weblate.org/widgets/kord-extensions/-/main/287x66-grey.png)](https://hosted.weblate.org/engage/kord-extensions/)

Kord Extensions is an addon for the excellent [Kord library](https://github.com/kordlib/kord). It intends to provide a
framework for larger bot projects, with easy-to-use commands, rich argument parsing and event handling, wrapped up
into individual extension classes.

The approach taken here is relatively different from a lot of Kotlin libraries, many of which prefer to provide a DSL
for quickly prototyping or implementing a small application. Instead,
[Discord.py](https://github.com/Rapptz/discord.py) (the Discord library for Python) is a primary source of inspiration
for our fairly object-oriented design, especially where it comes to its extensions (which are known as cogs in  
Discord.py). Despite this, we still strive to provide an idiomatic API that makes full use of Kotlin's niceties.

If you're ready to get started, please [take a look at the documentation](https://kordex.kotlindiscord.com/).

---

# Development Testing

If you're a contributor (current or future), you'll need to be testing your code. While we do encourage that you write
unit tests, we also ask that you use the test bot as a form of integration test. If you break the test bot, then
you've broken KordEx too!

You can find the test bot in the `test-bot` module. To run it, use Gradle to run the `test-bot:run` task, with the
following environment variables set:

* `TEST_SERVER` - your test server's ID
* `TOKEN` - your testing bot's token

Optionally, you can provide the following environment variables:

* `ENVIRONMENT` - Set this to `spam` to enable trace logging for Kord's gateway
* `LOG_LEVEL` - One of `ERROR`, `WARNING`, `INFO` or `DEBUG`, which refers to the highest log level that will be posted
  in `#test-logs` (as mentioned below)
* `PLURALKIT_TESTING` - Set this to any value (eg, `true`) to enable the PluralKit integration test module, which will
  respond to all message events (create, delete, update) with whether the message was proxied by PK or not.

Additionally, ensure that your test server contains a channel named `test-logs` that your test bot can send messages
to.

The test bot is intended for testing KordEx's complex systems, such as command handling, plugin loading and extension
management. If you modify any of these systems (or add new ones), it's important that you update (or add) extensions
that test them.

This module also provides some convenience functions that you can use when writing tests in commands and event 
handlers.
