# Module kord-extensions

Kord Extensions is a command and plugin library for [Kord](https://kordlib.github.io/kord/). It provides a ton of useful tools for writing Discord bots with Kord, including an extensions framework, filterable event handling, and an extensible commands system with advanced parsing features.

# Package com.kotlindiscord.kord.extensions

Base package for Kord Extensions, containing exceptions, the `Paginator`, and the `ExtensibleBot` itself.

# Package com.kotlindiscord.kord.extensions.checks

Check functions, which can be used to easily filter commands and event handlers.

# Package com.kotlindiscord.kord.extensions.commands

All the command logic, including the different types of `Command`s and `CommandContext`.

# Package com.kotlindiscord.kord.extensions.commands.converters

The base and convenience classes for command argument converters.

# Package com.kotlindiscord.kord.extensions.commands.converters.impl

Bundled type-specific implementations of converters.

# Package com.kotlindiscord.kord.extensions.commands.parser

The `ArgumentParser` and other argument-related classes.

# Package com.kotlindiscord.kord.extensions.events

Extra event types that you can listen for.

# Package com.kotlindiscord.kord.extensions.extensions

Extension classes, including the base `Extension` class, and the bundled `HelpExtension`.

# Package com.kotlindiscord.kord.extensions.parsers

Package containing bundled parsers for you to use, including the duration parsers.

# Package com.kotlindiscord.kord.extensions.utils

Many useful utilities that you can use when writing bots.

# Package com.kotlindiscord.kord.extensions.utils.deltas

Utilities and delta objects for calculating the difference between different objects of the same type.
