# Module kord-extensions

Kord Extensions is a command and plugin library for [Kord](https://kordlib.github.io/kord/). It provides a ton of useful
tools for writing Discord bots with Kord, including an extensions framework, filterable event handling, and an
extensible commands system with advanced parsing features.

# Package dev.kordex.core

Base package for Kord Extensions, containing exceptions, the `Paginator`, and the `ExtensibleBot` itself.

# Package dev.kordex.core.checks

Check functions, which can be used to easily filter commands and event handlers.

# Package dev.kordex.core.commands

All the command logic, including the different types of `Command`s and `CommandContext`.

# Package dev.kordex.core.commands.converters

The base and convenience classes for command argument converters.

# Package dev.kordex.core.commands.converters.impl

Bundled type-specific implementations of converters.

# Package dev.kordex.core.commands.parser

The `ArgumentParser` and other argument-related classes.

# Package dev.kordex.core.events

Extra event types that you can listen for.

# Package dev.kordex.core.extensions

Extension classes, including the base `Extension` class, and the bundled `HelpExtension`.

# Package dev.kordex.core.parsers

Package containing bundled parsers for you to use, including the duration parsers.

# dev.kordex.core.sentry

Package containing code related to the Sentry integration.

# Package dev.kordex.core.utils

Many useful utilities that you can use when writing bots.

# Package dev.kordex.core.utils.deltas

Utilities and delta objects for calculating the difference between different objects of the same type.
