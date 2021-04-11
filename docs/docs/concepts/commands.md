# Commands

If you want your bot to interact with users in a more complex manner than simply reacting to events, then you'll
probably want to write some commands. Kord Extensions provides a robust and flexible commands system, built around
a custom argument parsing solution that allows you to specify the arguments for your command in an entirely type-safe
manner.

We support both standalone and grouped commands, and you can even create your own specialised command types if you
wish. That said, it's usually best to start with the basics.

## The basics

Commands provide a way for you to specify a set of actions the bot can take on a user's behalf, specified using a
prefixed message in a Discord channel or private message. Creating one is simple - just use the `command` function in 
your extension's `setup` function.

```kotlin
command {  // this: Command
    name = "ping"  // Command name
    aliases = arrayOf("pong")  // Alternate command names
    description = "Returns 'Pong!'"  // Help text for the help command

    check { event -> true }  // Return `false` to prevent the action
    check(::returnTrue)  // You can also pass it a function
    
    action {  // this: MessageCommandContext
        message.respond("Pong!")
    }
}
```

Use the `check` function to define a set of predicates that must all return `true` in order for the command's action to
be run - you can read more about checks on [the Checks page](/concepts/checks). Once all the checks pass, the `action`
lambda will be called.

Note that the `action` lambda above is a receiver function, where `this` is bound to a `MessageCommandContext` object.

### Options

Commands have quite a lot of options that you may be interested in while setting one up. The following properties
are available right in the `command` lambda - please note that properties marked with :warning: are required and must
be set in order to properly register a command.

Name          | Type             | Description
:------------ | :--------------: | :----------
`name`        | `String`         | :warning: The primary name of the command, which must be unique throughout the bot and is used for invocation
`aliases`     | `Array <String>` | An array of secondary names to use for this command, which are also used for invocation and help commands
`description` | `String`         | A long description used by the help extension to describe the command - the first line of which should be a short summary
`enabled`     | `Boolean`        | Defaulting to `true`, this can be set programmatically in order to entirely disable or re-enable the command
`hidden`      | `Boolean`        | Default to `false`, this can be set to `true` to completely hide the command from the help extension's command listings
`signature`   | `String`         | A string representing the arguments for the command, automatically-generated from the command's arguments by default

Additionally, the following functions are available - please note that functions marked with :warning: are  required 
and must be called in order to properly register a command.

Name                 | Description
:------------------- | :----------
`action`             | :warning: A DSL function allowing you to define the code that will be run when the command is invoked, either as a lambda or by passing a function reference
`check`              | A function allowing you to define one or more checks for this command - see [the Checks page](/concepts/checks) for more information
`requirePermissions` | If your command requires the bot to have some permissions, specify them here and an error will be returned then the command is run when the bot doesn't have all of those permissions

### Command context

A `MessageCommandContext` object is a light wrapper around the command invocation, and it exists only for the duration 
of your command's `action`. It exists to provide a little extra context and functionality for your command body

`MessageCommandContext` objects expose the following properties.

Name          | Type                       | Description
:------------ | :------------------------: | :----------
`argList`     | `Array <String>`           | A simple array of string arguments that were passed to this command invocation
`argString`   | `String`                   | String representing the command invocation's arguments, exactly how they were sent on Discord
`arguments`   | `T`                        | Arguments object containing the command's parsed arguments, as described below
`breadcrumbs` | `MutableList <Breadcrumb>` | List of Sentry breadcrumbs, for the [Sentry intgration](/integrations/sentry)
`channel`     | `MessageChannelBehavior`   | The message channel this command happened in
`command`     | `Command`                  | Current command being invoked - note that this is a generic type, and you'll need to cast it to `MessageCommand`
`commandName` | `String`                   | Name of the current command being invoked, as provided by the user and lower-cased (meaning that this will be an alias if the user called the command that way)
`event`       | `MessageCreateEvent`       | MessageCreateEvent that is responsible for causing this command invocation
`guild`       | `Guild?`                   | The guild this command happened in, if any
`member`      | `Member?`                  | Guild member responsible for executing this command, if it happened in a guild and the user wasn't actually a webhook
`message`     | `Message`                  | Message object from the `event`
`user`        | `User?`                    | User responsible for executing this command, if the user wasn't actually a webhook

Additionally, `MessageCommandContext` objects expose the following functions.

Name         | Description
:----------- | :----------
`breadcrumb` | Convenience function to create and add a Sentry breadcrumb, for the [Sentry intgration](/integrations/sentry).
`sendHelp`   | Attempts to respond with command help using the loaded help extension, returning `false` if no such extension is loaded. Help extensions implement the `HelpProvider` interface.

### Command arguments

Kord Extensions provides an object-based approach to argument parsing, using a base `Arguments` type and delegation to
extension functions. Defining the arguments for your command is fairly simple:

```kotlin
class PostArguments : Arguments() {
    // Single required string argument
    val title by string("title", "Post title")

    // Single required Discord user argument
    val author by user("author", "User that this post should be attributed to")

    // Consumes the rest of the arguments into a single string
    val body by coalescedString("body", "Text content to be placed within the posts's body")
}
```

We recommend writing this as an inner class, but it's up to you where you put it (as long as it's public). Once you've
created your argument class, you can start using it in your commands:

```kotlin
command(::PostArguments) {
    name = "post"
    description = "Create a post"

    action {
        // Option 1
        message.respond(
            "**${arguments.title}** (by ${arguments.author.mention})\n\n" +
                arguments.body
        )

        // Option 2
        with(arguments) {
            message.respond(
                "**$title** (by ${author.mention})\n\n" +
                    body
            )
        }
    }
}
```

The argument parser will validate the command arguments for you automatically - if there's a problem, a
`CommandException` will be thrown. These exceptions are automatically handled by the bot, and will be returned to
whoever invoked the command to let them know what exactly went wrong.

For more information on how exactly this parsing works (and how to customize it), please see 
[the Converters page](/concepts/converters).

## Command groups

For ease of use, we also provide a special type of command that includes the ability to nest commands within it.
Grouped commands work just like regular commands, but with a few differences:

* Grouped commands have a default action that informs the user of the available subcommands, which means you don't have
  to define an `action` unless you want to override this behaviour
* Grouped commands have their own `command` and `group` functions that can be used for nesting
* Checks are tested while traversing the tree, which means that a parent's checks must pass for a subcommand to execute

The help extension also fully supports subcommands, and understands what you meant if you try to request help for a
subcommand - just provide the full command, and you're good to go.

```kotlin
group {
    name = "sport"
    description = "Set your favourite sport!"
    
    command {
        name = "basketball"
        description = "Set your favourite sport to basketball"
        
        action {
            message.respond("Your favourite sport is now basketball.")
        }
    }
    
    command {
        name = "football"
        description = "Set your favourite sport to football"
        
        action {
            message.respond("Your favourite sport is now football.")
        }
    }
    
    group {
        name = "craft"
        description = "Sports are boring, pick a craft instead!"
        
        command {
            name = "knitting"
            description = "Set your favourite sport to knitting"

            action {
                message.respond("Your favourite sport is now knitting.")
            }
        }
        
        command {
            name = "painting"
            description = "Set your favourite sport to painting"

            action {
                message.respond("Your favourite sport is now painting.")
            }
        }
    }
}
```

## Command parsing

While not everyone will need to understand precisely how commands are parsed under the hood, it's worth exploring
some specifics - you'll need to understand how to specify arguments on Discord, and your users will likewise need
an understanding of this.

Kord Extensions supports two ways to supply arguments out of the box: Positional arguments, and keyword arguments.
You can mix both approaches; arguments will always be parsed in the order they're defined in the `Arguments` subclass,
but keyword arguments can appear anywhere in the command (aside from inside another argument). Positional arguments are
always parsed in order.

??? missing "Not Implemented: Flags"
    Occasionally, a user will ask us why our parser doesn't support Unix-style flags - for example, `--argument` or
    `-a value`. The primary reason for this is that the current argument parser is already fairly complex, and 
    supporting flags within it would massively increase the maintenance burden it already carries.

    Additionally, while developers and Linux users will be very familiar with flags, we don't feel that most users
    will find them as simple to understand as our current keyword arguments. That said, you're always free to subclass
    the `ArgumentParser` class and implement your own parsing - if you do anything interesting with this, please let
    us know!

Additionally, single arguments can contain spaces if you `"surround them in quotes"`. As an example, take the 
following command:

```markdown
!post author=109040264529608704 "This is my title" **My Post**

This is part of the body of my post, despite being a couple lines down.
Arguments can happily contain newlines - although arguments can't be 
*separated* using newlines, so be careful!
```

Assuming a command prefix of `!`:

1. Our command is named `post`
1. Next, a keyword argument referring to `author` is found, and a reference is stored
1. `title` is parsed first, into the string `"This is my title"`
1. Next, `author` is parsed - the bot will search for a user with the given ID, but a mention or `user#discrim` string can also be provided instead
1. Finally, `body` is parsed - it's a coalescing string converter, so it consumes the rest of the arguments

This is a fairly simple example, and you can write some fairly complicated command handling if you feel so inclined.

??? tip "Consuming multiple arguments"
    Multi and coalescing converters consume arguments until they come across something they can no longer consume - for
    example, the `numberList` extension function returns a `MultiConverter` that consumes whole numbers until it no 
    longer can. It will consume  numbers from its starting argument onwards, until it encounters something that isn't 
    a number. At that point, it stops and tells the argument parser how many arguments have been consumed, and 
    processing continues to the next converter.

??? tip "Extensible converters"
    Converters themselves are quite extensible, and you shouldn't be afraid to write custom converters for types that
    are unique to your bot. Additionally, the order of the arguments defined in your `Arguments` subclass will be
    matched by the parser, so it is technically possible to create a converter that, for example, takes a lambda that
    relies on the value of a previously specified argument.

## Custom command types

Kord Extensions provides three command types (`MessageCommand`, `MessageGroupCommand` and `MessageSubCommand`), which 
should be fairly self-explanatory. You can also create your own command types by subclassing `MessageCommand` and 
overriding different properties and functions. We also support Discord's slash commands - you can read about them in 
a later section on this page.

Some properties you may be interested in include:

Name        | Type                                          | Description
:---------- | :-------------------------------------------: | :----------
`body`      | `MessageCommandContext <T>.()`                | The command body, normally set using the `action` function
`checkList` | `MutableList <MessageCreateEvent -> Boolean>` | A list of check predicates required for command execution
`parser`    | `ArgumentParser`                              | Class in charge of handling argument parsing, which you can override if you'd like to change how that works

The following functions may also be interesting for subclasses:

Name        | Description
:---------- | :----------
`runChecks` | Function in charge of running all the checks and reporting its success
`call`      | Function called to handle the actual command invocation - a lot of things happen here, you'll want to read the source before overriding this

As the command system is still constantly evolving, you'll want to keep up with releases and double-check for breakages
in your custom command types. Despite this maintenance burden, custom command types have a lot of uses - so feel free
to experiment!

## Slash commands

At some point, Discord came up with the idea of integrating slash commands with the Discord client. Kord Extensions
supports slash commands as well, via the `slashCommand` function and `SlashCommand` class. Usage is very similar to
the message-based commands above, with a few notable changes.

```kotlin
slashCommand(::PostArguments) {
    name = "post"
    description = "Commands for working with posts"
    
    subCommand(::GetArguments) {
        name = "get"
        description = "Get a post by title"

        action {
            val post = getPostByTitle(arguments.title)

            followUp {
                content = "**${post.title}** (by ${post.author.mention})\n\n" +
                    post.body
            }
        }
    }
    
    subCommand(::CreateArguments) {
        name = "create"
        description = "Create a new post"

        action {
            followUp {
                content = "**${arguments.title}** (by ${arguments.author.mention})\n\n" +
                    arguments.body
            }
        }
    }
}
```

Slash commands support the following options. They can be set directly within the `slashCommand` lambda - please 
note that the properties marked with :warning: are required and must be set in order to properly register a command.

Name          | Type             | Description
:------------ | :--------------: | :----------
`autoAck`     | `AutoAckType`    | Whether to automatically ack with an ephemeral (`EPHEMERAL`) or public (`PUBLIC`) acknowledgement, or none at all (`NONE`) if you want to do it yourself
`name`        | `String`         | :warning: The primary name of the command, which must be unique throughout the bot and is used for invocation
`description` | `String`         | :warning: A description for this command, which will be shown to users on Discord

??? note "Command groups and subcommands"
    Subcommands (whether inside a command group or directly within a slash command) work just like regular slash commands do, with the following caveats:
    
    * Subcommands may not be guild-limited, and setting the `guild` property will result in an error. Instead, you'll 
      have to limit the root command, and all subcommands will be similarly limited.
    * Subcommands run the root command's checks first, if any - both the root command's checks and the subcommand's 
      checks must pass for the command to be run.
    
    You may only have 25 command groups (with a max of 25 subcommands per group) per command. If you're using top-level 
    subcommands, each slash command may only have 25 of those.
    
    On a top-level command, you may only have an action, some command groups, **or** some subcommands. Providing more 
    than one of these will result in an error - exactly one must always be provided. Additionally, subcommands may not
    have their own subcommands or command groups.

Additionally, the following functions are available. Please note that all slash commands require at least one call to
either `action`, `group` or `subcommand`, but you may only pick one - these options are mutually exclusive.

Name         | Description
:----------- | :----------
`action`     | A DSL function allowing you to define the code that will be run when the command is invoked, either as a lambda or by passing a function reference
`check`      | A function allowing you to define one or more checks for this command - see [the Checks page](/concepts/checks) for more information
`group`      | A function allowing you to create a named subcommand group that will be shown on Discord. Just like slash commands, command groups require you to set a description - don't forget to!
`guild`      | A function allowing you to specify a specific guild for this command to be restricted to, if you don't want it to be registered globally
`subCommand` | A function allowing you to create a subcommand, either directly within the top-level command or within a command group.

If your slash command has no arguments, simply omit the argument builder parameter.

??? note "Slash commands and response types"
    Slash commands are quite beta, at least as far as Discord is concerned. There are some strange behaviours that are
    difficult to understand, so we've done our best to make sure things work as intuitively as we can.

    When you create a slash command and set the `autoAckType`, you're expected to use the corresponding `followUp`
    function when sending responses to your slash command. If you're using an ephemeral style for your reponses,
    remember that you can (often) send a normal message if you want to show something in public.

    Until Discord makes things work more sensibly, we will not be supporting the `response` API. If you need to use
    this in the meantime, you can set the `autoAckType` to `NONE` and make use of Kord's interaction API that's
    exposed on the `event` object directly.
