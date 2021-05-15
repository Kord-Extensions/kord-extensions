# General

To make your life easier, Kord Extensions contains a number of utilities. These utilities have been categorized by
type, to make things easier to find.

??? question "Why not PR these to Kord?"
    At its core, Kord is a protocol library and caching implementation. It's designed to follow Discord's API spec
    as close as it can and, while it does provide many niceties, not every utility makes sense to include directly
    within Kord.

    If you feel like that's not the way things should be, please have a chat with the Kord team - we're always working
    with them and we'll happily share whatever utilities they feel should be upstream!

## Channels

Function        | Description
:-------------- | :----------
`ensureWebhook` | Ensures that a webhook with the given parameters exists in a channel, and returns it for use

## Deltas

Kord itself doesn't contain any real way to compare two events or objects, so Kord Extensions includes some deltas
you can use to find the difference between certain objects.

* `UserDelta` for comparing two base `User` objects
* `MemberDelta` for comparing two guild `Member` objects
* `MessageDelta` for comparing two guild `Message` objects

In all cases, you should use the static `from` function to create a delta object. All properties on delta objects are
Kord `Optional`s - they'll be set to `Optional.Missing` if there was no change between the two objects, otherwise
they'll contain the **newly changed** value.

If the `from` function returns `null`, then the `old` object you passed as the first parameter is also `null`.

## Environment

Function  | Description
:-------- | :----------
`env`     | Get the value for an environmental variable (if it exists), attempting to load from a `.env` file in the current working directory first.

## Koin

Function     | Description
:----------- | :----------
`getKoin`    | For situations where you can't inherit the `KoinComponent` interface (for example, in a top-level function), you can use this function to get the current Koin instance.
`loadModule` | Wrapper around Koin's `module` DSL function that immediately registers the module against the current Koin instance.

## Kord

Property     | Description
:----------- | :----------
`Kord.users` | Quick access to a `Flow` containing all Kord `User` objects that are in the cache

Function                 | Description
:----------------------- | :----------
`Kord.waitFor`           | Simple function for waiting for a specific Kord event, with a timeout
`KordLiveEntity.waitFor` | Simple function for waiting for a specific Kord event that refers to the given live entity, with a timeout

## Members

Function                | Description
:---------------------- | :----------
`Member.hasRole`        | Checks whether a guild member has the given role
`Member.hasRoles`       | Checks whether a guild member all of the given roles
`Member.hasPermission`  | Checks whether a guild member has the given permission at guild level
`Member.hasPermissions` | Checks whether a guild member all of the given permissions at guild level
`Member.getTopRole`     | Returns the guild member's top role, or `null` if they don't have a role

## Messages

Property                         | Description
:------------------------------- | :----------
`MessageData.authorId`           | Quick access to the message author's ID
`MessageData.authorIsBot`        | Quick access to check whether the message author is a bot
`Message.isCrossPost`            | Whether this message was sent from a different guild's followed announcement channel
`Message.isEphemeral`            | Whether this is an ephemeral message from the Interactions system
`Message.isPublished`            | Whether this message was published to the guilds that are following its channel
`Message.isUrgent`               | Whether this message came from Discord's urgent message system
`Message.originalMessageDeleted` | When `isCrossPost`, whether the source message has been deleted from the original guild
`Message.suppressEmbeds`         | Whether this message's embeds should be serialized

Function                         | Description
:------------------------------- | :----------
`Message.addReaction`            | Convenience function to add a Unicode emoji reaction represented by a string to the message
`Message.deleteReaction`         | Convenience function to remove a Unicode emoji reaction represented by a string or other relevant object from the message
`Message.deleteOwnReaction`      | Convenience function to remove a reaction from the message that was previously added by the bot
`Message.deleteIgnoringNotFound` | Convenience function to delete a message and ignore any errors if the message no longer exists
`Message.delete`                 | Convenience function to delete a message after a delay, which **does not block** the current coroutine or thread
`Message.getUrl`                 | Convenience function to construct the URL for a message
`Message.parse`                  | Parses message content into a list using an Apache Commons `StringTokenizer`, which supports quotes for individual values with spaces
`Message.requireChannel`         | Function that ensures a message was sent within a given channel (with some options), responding with an error if not
`Message.requireGuildChannel`    | Function that ensures a message was not sent privately (with some options), responding with an error if it was
`Message.respond`                | Convenience function to respond to a user's message, with the option to use either Discord's replies feature or just a mention

## Misc

Function                 | Description
:----------------------- | :----------
`runSuspended`           | Convenience function to run a block of code in a coroutine dispatcher, defaulting to `Dispatchers.IO` - useful for otherwise blocking calls

## Reaction

This utility transforms other types to `ReactionEmoji` objects.

Function                | Description
:---------------------- | :----------
`GuildEmoji.toReaction` | Transform the given `GuildEmoji` object into a `ReactionEmoji` object
`String.toReaction`     | Transform a `String` object containing a Unicode emoji into a `ReactionEmoji` object

## RestRequest

This utility concerns Ktor's `RestRequestException` objects.

Function                                | Description
:-------------------------------------- | :----------
`RestRequestException.hasStatus`        | Check whether the given exception concerns a specific `HttpStatusCode`
`RestRequestException.hasStatusCode`    | Check whether the given exception concerns a specific HTTP status code integer
`RestRequestException.hasNotStatus`     | Check whether the given exception **does not concern** a specific `HttpStatusCode`
`RestRequestException.hasNotStatusCode` | Check whether the given exception **does not concern** a specific HTTP status code integer

## Scheduler

The `Scheduler` class is a rudimentary approach to scheduling `Job`s within a given coroutine scope, with the option
to cancel the `Job` early if required. The following instance functions are available for this:

Function    | Description
:---------- | :----------
`cancelAll` | Immediately cancel the execution of all scheduled jobs
`cancelJob` | Given a job's UUID, cancel its execution
`finishAll` | Immediately execute the callbacks of all scheduled jobs
`finishJob` | Given a job's UUID, immediately execute its callback
`getJob`    | Given a job's UUID, return the `Job` object - or `null` if the UUID doesn't match anything
`schedule`  | Schedule a job - either returning a generated UUID, or using a provided one

## String

Function                 | Description
:----------------------- | :----------
`String.parseBoolean`    | Parses a String into a Boolean based on its starting character and the locale for the given context (or a given locale)
`String.splitOn`         | Splits a string into a Pair containing the characters matching the predicate up until its first failure, and the rest of the string

## Time

Function             | Description
:------------------- | :----------
`Duration.toSeconds` | For **a Time4J Duration**, return the total number of seconds it contains
`Duration.toHuman`   | For **a Java Time Duration**, return a human-readable string in the form `a days, b hours, c minutes and d seconds` (omitting zero-values)

## Users

Property           | Description
:----------------- | :----------
`User.createdAt`   | Quick access to a user's creation time, taken from their `Snowflake`
`User.profileLink` | Quick access to a user's profile URL

Function            | Description
:------------------ | :----------
`User.dm`           | Send a private message to a user, returning a `Message` object if successfull or `null` if the user has their private messages disabled
`topRole`           | Retrieve a user's top role, given a guild's `Snowflake`
`User?.isNullOrBot` | Check whether the given `User` object is null, or a bot
