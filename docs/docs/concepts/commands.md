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
    
    action {  // this: CommandContext
        message.respond("Pong!")
    }
}
```

Use the `check` function to define a set of predicates that must all return `true` in order for the command's action to
be run - you can read more about checks on [the Checks page](/concepts/checks). Once all the checks pass, the `action`
lambda will be called.

Note that the `action` lambda above is a receiver function, where `this` is bound to a `CommandContext` object.

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
`signature`   | `String`         | A string representing the arguments for the command - you can have this automatically generated using the `signature()` function as described later on in this document

Additionally, the following functions are available - please note that functions marked with :warning: are  required 
and must be called in order to properly register a command.

Name        | Description
:---------- | :----------
`action`    | :warning: A DSL function allowing you to define the code that will be run when the command is invoked, either as a lambda or by passing a function reference
`check`     | A function allowing you to define one or more checks for this command - see [the Checks page](/concepts/checks) for more information
`signature` | A function that will generate the command signature for you, when passed a function or constructor reference that returns an Arguments object, which will be described later on in this document

### Command context

A `CommandContext` object is a light wrapper around the command invocation, and it exists only for the  duration of 
your command's `action`. It exists to provide a little extra context and functionality for your command body

`CommandContext` objects expose the following properties.

Name          | Type                       | Description
:------------ | :------------------------: | :----------
`args`        | `Array <String>`           | A simple array of string arguments that were passed to this command invocation
`breadcrumbs` | `MutableList <Breadcrumb>` | List of Sentry breadcrumbs, for the [Sentry intgration](/integrations/sentry)
`command`     | `Command`                  | Current command being invoked 
`event`       | `MessageCreateEvent`       | MessageCreateEvent that is responsible for causing this command invocation
`message`     | `Message`                  | Message object from the `event`

Additionally, `CommandContext` objects expose the following functions.

Name         | Description
:----------- | :----------
`breadcrumb` | Convenience function to create and add a Sentry breadcrumb, for the [Sentry intgration](/integrations/sentry).
`parse`      | Given a function or constructor reference that returns an Arguments object, parses the command's arguments and returns a filled Arguments object - see below for more information

### Command arguments

Kord Extensions provides an object-based approach to argument parsing, using a base `Arguments` type and delegation to
extension functions. Defining the arguments for your command is fairly simple:

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

We recommend writing this as an inner class, but it's up to you where you put it (as long as it's public). Once you've
created your argument class, you can start using it in your commands:

```kotlin
command {
    name = "post"
    description = "Create a post"
    
    signature(::PostArguments)
    
    action {
        // Option 1
        val parsed = parse(::PostArguments)
        
        message.respond(
            "**${parsed.title}** (by ${parsed.author.mention})\n\n" +
                parsed.body
        )
        
        // Option 2
        with(parse(::PostArguments)) {
            message.respond(
                "**$title** (by ${author.mention})\n\n" +
                    body
            )
        }
    }
}
```

The argument parser will validate the command arguments for you automatically - if there's a problem, a
`ParseException` will be thrown. These exceptions are automatically handled by the bot, and will be returned to
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

## Custom command types

Kord Extensions provides three command types (`Command`, `GroupCommand` and `SubCommand`), which should be fairly
self-explanatory. You can also create your own command types by subclassing `Command` and overriding different
properties and functions.

Some properties you may be interested in include:

Name        | Type                                          | Description
:---------- | :-------------------------------------------: | :----------
`body`      | `CommandContext.()`                           | The command body, normally set using the `action` function
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
