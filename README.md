# Kord Extensions

[![Ko-Fi badge](https://img.shields.io/badge/Donate-Buy_me_a_coffee-purple?style=for-the-badge&logo=ko-fi)](https://ko-fi.com/gsc)

[![Docs: Click here](https://img.shields.io/static/v1?label=Docs&message=Click%20here&color=7289DA&style=for-the-badge&logo=read-the-docs)](https://docs.kordex.dev/) [![Discord: Click here](https://img.shields.io/static/v1?label=Discord&message=Click%20here&color=7289DA&style=for-the-badge&logo=discord)](https://discord.gg/nYzQWcjAmK) <br />
[![Weblate project translated](https://img.shields.io/weblate/progress/kord-extensions?style=for-the-badge)]((https://hosted.weblate.org/engage/kord-extensions/))

[![Translation status](https://hosted.weblate.org/widgets/kord-extensions/-/main/287x66-grey.png)](https://hosted.weblate.org/engage/kord-extensions/)

Kord Extensions is an addon for the excellent [Kord library](https://github.com/kordlib/kord). It intends to provide a
framework for larger bot projects, with easy-to-use commands, rich argument parsing, and event handling, wrapped up
into individual extension classes.

The approach taken here is relatively different from a lot of Kotlin libraries, many of which prefer to provide a DSL
for quickly prototyping or implementing a small application. Instead,
[Discord.py](https://github.com/Rapptz/discord.py) (the Discord library for Python) is a primary source of inspiration
for our fairly object-oriented design, especially where it comes to its extensions (which are known as cogs in
Discord.py). Despite this, we still strive to provide an idiomatic API that makes full use of Kotlin's niceties.

If you're ready to get started, please [take a look at the documentation](https://docs.kordex.dev/).

# Patrons

Thanks to those that have donated to [support the project via Ko-Fi](https://ko-fi.com/gsc).
If you'd like to be listed for your donation here, please mention Kord Extensions and provide your GitHub username in
your donation message.

- [@ToxicMushroom](https://github.com/ToxicMushroom)

# Contributors

Thanks to everyone who's supported this project.
The below grid shows the avatars of this repository's top contributors.

<a href="https://github.com/kord-extensions/kord-extensions/graphs/contributors">
  <img
    alt="Image grid showing all contributors' avatars"
    src="https://contrib.rocks/image?repo=kord-extensions/kord-extensions&max=200"
  />
</a>

[contrib.rocks](https://contrib.rocks) provides the above grid.

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

We designed the test bot to test KordEx's complex systems, such as command handling, plugin loading, and extension
management.
If you modify any of these systems (or add new ones), it is important you update (or add) extensions that test them.

This module also provides some convenience functions that you can use when writing tests in commands and event
handlers.
