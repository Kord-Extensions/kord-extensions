# Home

Kord Extensions is an addon for the excellent [Kord library](https://github.com/kordlib/kord). It intends to provide a
framework for larger bot projects, with easy-to-use commands, rich argument parsing and event handling, wrapped up 
into individual extension classes.

The approach taken here is relatively different from a lot of Kotlin libraries, many of which prefer to provide a DSL 
for quickly prototyping or implementing a small application. Instead, 
[Discord.py](https://github.com/Rapptz/discord.py) (the Discord library for Python) is a primary source of inspiration 
for our fairly object-oriented design, especially where it comes to its extensions (which are known as cogs in  
Discord.py). Despite this, we still strive to provide an idiomatic API that makes full use of Kotlin's niceties.

??? summary "Why not kordx.commands?"
    Kord has released their own command framework, [kordx.commands](https://github.com/kordlib/kordx.commands). It's 
    a competent library, but it takes some very different approaches to solving the same problems Kord Extensions does. 
    Most  notably, it requires the use of [kapt](https://kotlinlang.org/docs/reference/kapt.html) and makes use of an
    annotation-based autowire system for getting things registered.

    In contrast, Kord Extensions provides a less magical approach that is more closely tied to object-oriented
    programming, and may be more suitable for embedding into other applications. At the end of the day, though, the
    choice is yours - both approaches have pros and cons, and it's worth checking both out to see what you like
    better!

## Usage

To make use of Kord Extensions, update your build script to add
`https://maven.kotlindiscord.com/repository/maven-snapshots/` as a Maven repository, and use 
`com.kotlindiscord.kord.extensions:kord-extneions:VERSION` as the Maven coordinate. For a list of available versions, 
[take a look at Nexus](https://maven.kotlindiscord.com/#browse/browse:maven-snapshots:com%2Fkotlindiscord%2Fkord%2Fextensions%2Fkord-extensions).

We do not currently have a strict semantic versioning system in place. We'll explore this later if needed, but right 
now we recommend pinning to the latest snapshot version number.

For more specific directions for individual build systems, [take a look at the Getting Started guide](/getting-started).

