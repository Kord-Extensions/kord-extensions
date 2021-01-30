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

**Note:** All checks in this category take a DSL-style lambda argument that returns the type specified in the table
below, or a `Snowflake` that should resolve to an entity of that type.

Name                    | Expected Type       | Description
:---------------------- | :-----------------: | :-------------------------------------------------------------------------------------
`channelHigherOrEqual`  | `Channel Behavior`  | Asserts that an event fired in a channel **higher than or equal to** the given channel
`channelHigher`         | `Channel Behavior`  | Asserts that an event fired in a channel **higher than** the given channel
`channelLowerOrEqual`   | `Channel Behavior`  | Asserts that an event fired in a channel **lower than or equal to** the given channel
`channelLower`          | `Channel Behavior`  | Asserts that an event fired in a channel **lower than** the given channel
`inCategory`            | `Category Behavior` | Asserts that an event fired within the given channel category
`inChannel`             | `Channel Behavior`  | Asserts that an event fired within the given channel
`notInCategory`         | `Category Behavior` | Asserts that an event **did not** fire within the given channel category
`notInChannel`          | `Channel Behavior`  | Asserts that an event **did not** fire within the given channel

### Channel types

Name             | Argument      | Description
:--------------- | :-----------: | :------------------------------------------------------------------------------
`channelType`    | `Channel Type` | Asserts that an event fired within a channel matching the given type
`notChannelType` | `Channel Type` | Asserts that an event **did not** fire within a channel matching the given type

### Guilds

**Note:** All checks in this category take a DSL-style lambda argument that returns the type specified in the table
below, or a `Snowflake` that should resolve to an entity of that type.

Additionally, the `anyGuild` and `noGuild` checks are not currently able to tell the difference between an event that
wasn't fired within a guild, and an event that fired within a guild the bot doesn't have access to, or that it can't 
get the GuildBehavior for (for example, due to a niche Kord configuration).

Name         | Expected Type     | Description
:----------- | :---------------: | :-------------------------------------------------------------
`anyGuild`   |                   | Asserts that an event fired within any guild
`inGuild`    | `Guild Behavior`  | Asserts that an event fired within the given guild
`noGuild`    |                   | Asserts that an event **did not** fire within a guild
`notInGuild` | `Guild Behavior`  | Asserts that an event **did not** fire within the given guild

### Members

??? important "Channel overwrites"
    Note that these checks currently only operate based on guild roles, and they ignore channel overwrites. If you
    need checks for channel overwrites then please let us know, and we'll try to figure out a good solution.

Name               | Argument     | Description
:----------------- | :----------: | :-------------------------------------------------------------------------------------
`hasPermission`    | `Permission` | Asserts that an event was fired by a user with the given permission
`notHasPermission` | `Permission` | Asserts that an event was fired by a user **without** the given permission

### Roles

**Note:** All checks in this category take a DSL-style lambda argument that returns the type specified in the table
below, or a `Snowflake` that should resolve to an entity of that type.

Name                    | Expected Type   | Description
:---------------------- | :-------------: | :-------------------------------------------------------------------------------------
`hasRole`               | `Role Behavior` | Asserts that an event was fired by a user with the given role
`notHasRole`            | `Role Behavior` | Asserts that an event was fired by a user **without** the given role
`topRoleEqual`          | `Role Behavior` | Asserts that an event was fired by a user with a top role that matches the given role
`topRoleHigherOrEqual`  | `Role Behavior` | Asserts that an event was fired by a user with a top role **higher than or equal to** the given role
`topRoleHigher`         | `Role Behavior` | Asserts that an event was fired by a user with a top role **higher than** the given role
`topRoleLowerOrEqual`   | `Role Behavior` | Asserts that an event was fired by a user with a top role **lower than or equal to** the given role
`topRoleLower`          | `Role Behavior` | Asserts that an event was fired by a user with a top role **lower than** the given role
`topRoleNotEqual`       | `Role Behavior` | Asserts that an event was fired by a user with a top role that **does not** match the given role

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

As interaction events generally only contain `Snowflake`s, most of the below functions will query the Kord cache or REST
when dealing with them.

Name                  | Return Type        | Notes
:-------------------- | :----------------: | :-------------------------------------------------------------------------------------
`channelFor`          | `Channel Behavior` | 
`channelIdFor`        | `Long`             | This function will return a `Long` taken from a `Snowflake`, rather than the `Snowflake` itself
`channelSnowflakeFor` | `Snowflake`        | This function is just like the previous, but will not unwrap the `Snowflake`
`guildFor`            | `Guild Behavior`   | This function will query the Kord cache or REST for `MessageCreateEvent` and `MessageUpdateEvent`
`memberFor`           | `Member Behavior`  | This function will query the Kord cache or REST for all supported events except for `MemberJoinEvent` and `MemberUpdateEvent`
`messageFor`          | `Message Behavior` | This function will query the Kord cache or REST for `MessageUpdateEvent`
`roleFor`             | `Role Behavior`    | 
`userFor`             | `User Behavior`    | This function will query the Kord cache or REST for `MessageUpdateEvent`, and will only return the first user that isn't the bot for `DMChannelCreateEvent`, `DMChannelDeleteEvent` and `DMChannelUpdateEvent` (as we don't support selfbots)

## Creating reusable checks

??? warning "Checks concerning entities: Always resolve late!"
    If you're writing a check that concerns a Kord entity - for example, a `GuildBehavior` or `ChannelBehavior` (or,
    indeed, `Guild` or `Channel`), it's important that you provide two very specific implementations:
    
    * A version that takes a lambda returning the entity you're checking, which **is called within an inner function
      that is returned by your outer check function**.
    * A version that takes a `Snowflake` and wraps the lambda version you wrote above.
    
    The reason this is required is to ensure that extensions making use of these checks do not try to resolve entities
    before the bot has connected to Discord. If you require your user to directly pass entities to your check functions,
    or your check functions immediately try to resolve entities by calling the passed lambda too early, those
    extensions will throw exceptions when they're being registered.
    
    Here's an example: 

    ```kotlin
    public fun inChannel(builder: suspend () -> ChannelBehavior): suspend (Event) -> Boolean {
        val logger = KotlinLogging.logger("com.kotlindiscord.kord.extensions.checks.inChannel")
    
        suspend fun inner(event: Event): Boolean {
            val eventChannel = channelFor(event)
        
           if (eventChannel == null) {
               logger.nullChannel(event)
               return false
           }
    
           val channel = builder()
    
           return if (eventChannel.id == channel.id) {
               logger.passed()
               true
           } else {
               logger.failed("Channel $eventChannel is not the same as channel $channel")
               false
           }
        }
    
        return ::inner
    }
    ```

All checks may be defined as functions, and all check functions will accept a function reference. We also provide a set
of logger extension functions to make creating consistent log messages easier.

As an example, it's common practise to define a "default" check that applies to most commands like the following:

```kotlin
suspend fun defaultCheck(event: Event): Boolean {
    val logger = KotlinLogging.logger("my.package.defaultCheck")
    val message = messageFor(event)?.asMessage()

    return when {
        message == null -> {
            logger.nullMessage()
            false
        }

        message.author == null -> {
            logger.failed("Message sent by a webhook or system message")
            false
        }

        message.author!!.id == bot.kord.getSelf().id -> {
            logger.failed("We sent this message")
            false
        }

        message.author!!.isBot -> {
            logger.failed("This message was sent by another bot")
            false
        }

        message.getChannelOrNull() is DmChannel -> {
            logger.passed("This message was sent in a DM")
            true
        }

        message.getGuildOrNull()?.id != config.getGuild().id -> {
            logger.failed("Not in the correct guild")
            false
        }

        else -> {
            logger.passed()
            true
        }
    }
}
```

If you've written a check that you think would be useful for most users (and it can be written in a generic
way) then feel free to open an issue or pull request, and we'll look at getting it added to the framework proper.
