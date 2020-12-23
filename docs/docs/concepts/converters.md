# Converters

Converters are small classes that exist to convert strings - or groups of strings - into other types. They make up
the core of the argument parsing provided with Kord Extensions, allowing you to easily parse command arguments into
complex types with no compiler plugins, generated Kotlin or reflection whatsoever.

## Converter types

In the interests of making sure you never get a type you're not expecting, Kord Extensions ships with a wide variety
of converters. Converters are first classified by the way they behave, and then by the type they exist to convert
to.

The basic converter types are as follows:

Type                  | Description
:-------------------: | :----------
`CoalescingConverter` | A converter representing a required argument converted from a list of strings, combined into a single value
`DefaultingConverter` | A converter representing a single argument with a default value, converted from up to one supplied string
`MultiConverter`      | A converter representing an argument converted from a list of strings, one value per string - which may be either required or optional
`OptionalConverter`   | A converter representing a single, optional/nullable argument converted from up to one supplied string, with an optional `outputError` property that will fail the parse and return an error if there was a problem during parsing
`SingleConverter`     | A converter representing a single, required argument converted from exactly one supplied string

There are also some compound converters, which combine the behaviours found in other converters, such as: 

* `DefaultingCoalescingConverter`
* `OptionalCoalescingConverter`

We also provide some special implementations that wrap other converters, such as:

* `CoalescingToDefaultingConverter` (obtained via `CoalescingConverter#toDefaulting()`)
* `CoalescingToOptionalConverter` (obtained via `CoalescingConverter#toOptional()`)
* `SingleToDefaultingConverter` (obtained via `SingleConverter#toDefaulting()`)
* `SingleToMultiConverter` (obtained via `SingleConverter#toMulti()`)
* `SingleToOptionalConverter` (obtained via `SingleConverter#toOptional()`)

We recommend exploring the source code for these converters, as we're likely to continue adding them.

## Bundled converters

Converters are provided that support the following type conversions, out of the box:

* `Boolean`
* `Channel`
* `Decimal` (Doubles only)
* `Duration` (Java Time and Time4J) with special-casing for coalescing conversion
* `Email`
* `Emoji` (Server emoji on Discord)
* `Enum` (Any enums you like, including those you define yourself)
* `Guild`
* `Member`
* `Message`
* `Number` (Longs only)
* `Regex` (Kotlin wrapper type only) with special-casing for coalescing conversion
* `Role`
* `String` with special-casing for coalescing conversion
* `User`

## Usage

Converters are intended to be used as part of your commands setup, via the `Arguments` object. `Arguments` is a special
type that contains a list of each of the delegated properties in your class, in order - placed there by 
specially-created extension functions that create converters for you. Here's an example, from 
[the Commands page](/concepts/commands).

```kotlin
class PostArguments : Arguments() {
    // Single required string argument
    val title by string("title")

    // Single required Discord user argument
    val author by user("author")

    // Consumes the rest of the arguments into a single string
    val body by coalescedString("body")
}
```

In this example, we have three arguments:

* `title` - a required String argument, with the friendly name of "title"
* `author` - a required Discord User argument, with the friendly name of "author"
* `body` - A required coalescing String argument, with the friendly name of "body"

All arguments require a friendly name. This name will be used to refer to that argument in the command's signature,
as well as error and other help messages. It's also the name that will be used for keyword arguments.

On top of this, different converters (and types of converters) may take extra arguments. For example, all defaulting
converters require you to pass a default value to their creation function, while optional converters do not take
a default value and instead will return `null` by default.

??? tip "Finding converter functions"
    As of this writing, there are over 700 lines covering `Arguments` object extension functions. While you can read
    the generated Dokka documentation for a full list, we recommend making use of your IDE's auto-completion 
    functionality for discovery - especially if you've written custom converters, or you're making use of third-party
    converters.

## Custom converters

If you'd like to convert arguments to a type that we don't currently support, or you'd like to add some extra
validation to arguments, then custom converters are the way to go. Depending on what you're doing, you'll want
to start by subclassing one of the following converter base classes:

* `SingleConverter` for single converters, which can be wrapped into defaulting, multi and optional converters
* `CoalescingConverter` for coalescing converters, which can be wrapped into defaulting and optional variants

When creating your converter, one of the biggest things you can do to help yourself is to read over the Dokka
generated documentation for the base classes, and look at the source for the bundled converters. Custom converters can
be a little tricky to get your head around at first, so feel free to join the Kotlin Discord server if you need to ask
questions.

??? question "How do I bail when something goes wrong?"
    As you may expect, errors are handled using Kotlin's exceptions system. Exceptions can be thrown as normal, and
    they'll be caught by the argument parser and transformed into an error message. However, you may wish to provide
    a more useful error message to the user - for these cases, you should create and throw an instance of
    `ParseException` yourself. The message passed to `ParseException` will be returned to the user verbatim, so make
    it descriptive!

    **Remember that your end users are not necessarily developers!** Most people will not understand a technical
    description or a default exception message - any `ParseException` instances you throw should contain
    a human-readable error message that **tells the user what went wrong so that they can correct** their command
    invocation and try again. If you need to provide developer-oriented feedback, use a logger!

    Because of this, only `ParseException` instances will be returned to the user verbatim. All other exception types
    will be logged, and a generic error message will be returned to the user.

Once you've created your converters, we recommend writing `Arguments` extension functions for them. As before, we
heavily recommend reading the source for the extension functions that already exist - they're quite simple, but it 
always pays to try to write the best APIs you can, especially if you expect someone else to make use of your code 
someday!
