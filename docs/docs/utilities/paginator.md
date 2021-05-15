# Paginator

A fairly common use-case for bots is displaying pages of data - messages can only be so long, and posting too much
text at once can be fairly disruptive. To cover this use-case, Kord Extensions provides a fairly extensible paginator.
This allows you to customize the embeds used for each page, as well as specifying switchable page groups if you'd like
to - for example - allow users to expand the current page into something with more information.

The paginator adds a set of reactions below an embed, allowing users to flip between pages, skip to the first or last
page, switch between page groups or destroy the paginator when they're done with it.

The paginator includes a set of classes that abstract away a fair amount of the logic required, but you can
extend any of these classes and customize them to your liking. There are three major classes you'll need to understand:
`Page`, `Pages` and `Paginator`.

## Page

The `Page` class is a light container for embed data, and provides a method used to build the embed used to display
the page on Discord. It supports a number of parameters that correspond with embed properties, but only the first one -
`description` - is required.

As part of this formatting, the page number and current group will be inserted into the embed footer, if applicable.
For this reason, it's wise to keep the footer text for your pages short. Footer text is formatted like this,
`Page x/y • group • given footer text`, although you can customize this by overriding the `build` function in a 
`Page` subclass.

## Pages

The `Pages` class provides a container for your pages and their groups. When creating your pages, you should create
an instance of the `Pages` class (or a subclass) and add pages to it as you go, using the provided functions.

The `Pages` constructor takes a single argument, which is the name of the default group. If not specified, this will
be the empty string, `""`. A group named with the empty string will not be named in the embed footer, but you can
change this behaviour by providing a different default group name. The paginator will always inform users when there's
more than two groups.

!!! warning "Page collection validation"
    **Note:** In order to be considered valid, a collection of pages must meet the following criteria:

    * It must contain at least one group of pages
    * All page groups must contain the same number of pages

    If this isn't the case, the paginator will throw an exception when it's created.

The following functions are available for use.

Function   | Description
:--------- | :----------
`addPage`  | Add a page to this collection of pages, defaulting to the default group or using a specified group
`get`      | Get a page by index, defaulting to the default group or using a specified group
`validate` | Check that this collection of pages is valid, throwing if it isn't

## Paginator

The `Paginator` class ties all of this logic together in a single place, transforming collections of pages into 
actionable embeds on Discord. Currently, the paginator makes use of reactions directly on Discord, but this will
change once Discord provides better mechanisms for dealing with this kind of thing.

The first thing to do is create a `Paginator` instance, with the following constructor parameters. Please note that you **must** provide either `targetChannel` or `targetMessage`. If you supply both, `targetChannel` will take priority.

Name            | Type                     | Description
:-------------- | :----------------------: | :----------
`targetChannel` | `MessageChannelBehavior` | Channel to send the paginator within
`targetMessage` | `Message`                | Message to send the paginator in response to
`pages`         | `Pages`                  | Collection of pages to paginate
`pingInReply`   | `Boolean`                | When `targetMessage` is provided, whether to ping the message author in the reply, `true` by default
`owner`         | `User?`                  | If desired, provide a user here and no other users will be able to interact with the paginator
`timeout`       | `Long?`                  | If desired, amount of time with no reactions to wait before destroying the paginator
`keepEmbed`     | `Boolean`                | Whether to keep the embed when destroying the paginator, `true` by default
`switchEmoji`   | `ReactionEmoji`          | The emoji to use to cycle between page groups, :information_source: by default for two groups or :arrows_counterclockwise: for more than two
`locale`        | `Locale?`                | The Locale object to use for strings, which should be passed in from the `getLocale()` function in your command's context object

Once you've created your paginator, all you need to do is call the `send` function, and it'll be sent to the channel!
