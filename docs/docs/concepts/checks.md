# Checks

The checks system provides a way for you define and make use of predicates that decide whether an
[event handler](/concepts/events) or [command](/concepts/commands) should execute. The concept is relatively
simple - all checks receive an event, check whether execution should continue based on that event, and return a
Boolean value that decides what happens next - `true` to continue, `false` to stop.

Anything that makes use of checks will accept either a lambda, or a function reference. This allows you to create
reusable check functions that can be imported and made use of throughout your bot.

## Checks suite

Kord Extensions comes with a full suite of commonly-used checks. This suite includes checks that operate based on
specific types, combinator checks that provide boolean operations on sets of other checks, and a set of utilities
for writing your own checks or extracting information from Kord events.

??? important "Event not supported?"
    If an event is not supported by a check that you feel should be, please raise an issue with us - we'll look into
    it as quickly as we can. Kord does not provide generic interfaces for figuring out which events concern different
    types of entity, so we have to manually maintain utilities that match specific events by their types.

### Channels

Name                    | Argument   | Description
:---------------------- | :--------: | :-------------------------------------------------------------------------------------
`channelHigherOrEqual`  | `Channel`  | Asserts that an event fired in a channel **higher than or equal to** the given channel
`channelHigher`         | `Channel`  | Asserts that an event fired in a channel **higher than** the given channel
`channelLowerOrEqual`   | `Channel`  | Asserts that an event fired in a channel **lower than or equal to** the given channel
`channelLower`          | `Channel`  | Asserts that an event fired in a channel **lower than** the given channel
`inCategory`            | `Category` | Asserts that an event fired within the given channel category
`inChannel`             | `Channel`  | Asserts that an event fired within the given channel
`notInCategory`         | `Category` | Asserts that an event **did not** fire within the given channel category
`notInChannel`          | `Channel`  | Asserts that an event **did not** fire within the given channel

### Channel types

Name             | Argument      | Description
:--------------- | :-----------: | :------------------------------------------------------------------------------
`channelType`    | `ChannelType` | Asserts that an event fired within a channel matching the given type
`notChannelType` | `ChannelType` | Asserts that an event **did not** fire within a channel matching the given type

### Guilds

Name         | Argument | Description
:----------- | :------: | :-------------------------------------------------------------
`inGuild`    | `Guild`  | Asserts that an event fired within the given guild
`notInGuild` | `Guild`  | Asserts that an event **did not** fire within the given guild

### Roles

Name                    | Argument | Description
:---------------------- | :------: | :-------------------------------------------------------------------------------------
`hasRole`               | `Role`   | Asserts that an event was fired by a user with the given role
`notHasRole`            | `Role`   | Asserts that an event was fired by a user **without** the given role
`topRoleEqual`          | `Role`   | Asserts that an event was fired by a user with a top role that matches the given role
`topRoleHigherOrEqual`  | `Role`   | Asserts that an event was fired by a user with a top role **higher than or equal to** the given role
`topRoleHigher`         | `Role`   | Asserts that an event was fired by a user with a top role **higher than** the given role
`topRoleLowerOrEqual`   | `Role`   | Asserts that an event was fired by a user with a top role **lower than or equal to** the given role
`topRoleLower`          | `Role`   | Asserts that an event was fired by a user with a top role **lower than** the given role
`topRoleNotEqual`       | `Role`   | Asserts that an event was fired by a user with a top role that **does not** match the given role

### Combinators

Combinator checks operate on a variable number of other checks, transforming their results as appropriate.

Name  | Argument | Description
:---- | :------: | :-----------------------------------------------------------------
`and` | Checks   | Special check that passes if **any** of the given checks pass
`or`  | Checks   | Special check that passes only if **all** of the given checks pass

### Utilities

The following utilities are made use of in the other checks, but you can make use of them yourself if you need to try
to extract a given type from any event.

If an event is unsupported, then each of the below functions will return `null`. Please note that there are some events
that these functions do support that may themselves return a nullable value when queried.

Name                  | Return Type       | Notes
:-------------------- | :---------------: | :-------------------------------------------------------------------------------------
`channelFor`          | `ChannelBehavior` | 
`channelIdFor`        | `Long`            | This function will return a `Long` taken from a `Snowflake`, rather than the `Snowflake` itself
`channelSnowflakeFor` | `Snowflake`       | This function is just like the previous, but will not unwrap the `Snowflake`
`guildFor`            | `GuildBehavior`   | This function will query the Kord cache or REST for `MessageCreateEvent` and `MessageUpdateEvent`
`memberFor`           | `MemberBehavior`  | This function will query the Kord cache or REST for all supported events except for `MemberJoinEvent` and `MemberUpdateEvent`
`messageFor`          | `MessageBehavior` | This function will query the Kord cache or REST for `MessageUpdateEvent`
`roleFor`             | `RoleBehavior`    | 
`userFor`             | `UserBehavior`    | This function will query the Kord cache or REST for `MessageUpdateEvent`, and will only return the first user that isn't the bot for `DMChannelCreateEvent`, `DMChannelDeleteEvent` and `DMChannelUpdateEvent` (as we don't support selfbots)

## Creating reusable checks

All checks may be defined as functions, and all check functions will accept a function reference. As an example, it's
common practise to define a "default" check that applies to most commands like the following:

```kotlin
suspend fun defaultCheck(event: Event): Boolean {
    val logger = KotlinLogging.logger("my.package.defaultCheck")

    val message = messageFor(event)?.asMessage()

    return when {
        message == null -> {
            logger.debug { "Failing check: Message for event $event is null. This type of event may not be supported." }
            false
        }

        message.author == null -> {
            logger.debug { "Failing check: Message sent by a webhook or system message" }
            false
        }

        message.author!!.id == bot.kord.getSelf().id -> {
            logger.debug { "Failing check: We sent this message" }
            false
        }

        message.author!!.isBot -> {
            logger.debug { "Failing check: This message was sent by another bot" }
            false
        }

        message.getChannelOrNull() is DmChannel -> {
            logger.debug { "Passing check: This message was sent in a DM" }
            true
        }

        message.getGuildOrNull()?.id != config.getGuild().id -> {
            logger.debug { "Failing check: Not in the correct guild" }
            false
        }

        else -> {
            logger.debug { "Passing check" }
            true
        }
    }
}
```

If you've written a check that you think would be useful for the majority users (and it can be written in a generic
way) then feel free to open an issue or pull request, and we'll look at getting it added to the framework proper.
