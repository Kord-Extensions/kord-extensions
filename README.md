# Kord Extensions

[![Docs: Click here](https://img.shields.io/static/v1?label=Docs&message=Click%20here&color=7289DA&style=for-the-badge&logo=read-the-docs)](https://kordex.kotlindiscord.com/) [![Discord: Click here](https://img.shields.io/static/v1?label=Discord&message=Click%20here&color=7289DA&style=for-the-badge&logo=discord)](https://discord.gg/gjXqqCS) [![Build Status](https://img.shields.io/github/workflow/status/Kotlin-Discord/kord-extensions/CI/root?logo=github&style=for-the-badge)](https://github.com/Kotlin-Discord/kord-extensions/actions?query=workflow%3ACI+branch%3Aroot) <br />
[![Release](https://img.shields.io/nexus/r/com.kotlindiscord.kord.extensions/kord-extensions?nexusVersion=3&logo=gradle&color=blue&label=Release&server=https%3A%2F%2Fmaven.kotlindiscord.com&style=for-the-badge)](https://maven.kotlindiscord.com/#browse/browse:maven-releases:com%2Fkotlindiscord%2Fkord%2Fextensions%2Fkord-extensions) [![Snapshot](https://img.shields.io/nexus/s/com.kotlindiscord.kord.extensions/kord-extensions?logo=gradle&color=orange&label=Snapshot&server=https%3A%2F%2Fmaven.kotlindiscord.com&style=for-the-badge)](https://maven.kotlindiscord.com/#browse/browse:maven-snapshots:com%2Fkotlindiscord%2Fkord%2Fextensions%2Fkord-extensions)

Kord Extensions is an addon for the excellent [Kord library](https://github.com/kordlib/kord). It intends to provide a
framework for larger bot projects, with easy-to-use commands, rich argument parsing and event handling, wrapped up
into individual extension classes.

The approach taken here is relatively different from a lot of Kotlin libraries, many of which prefer to provide a DSL
for quickly prototyping or implementing a small application. Instead,
[Discord.py](https://github.com/Rapptz/discord.py) (the Discord library for Python) is a primary source of inspiration
for our fairly object-oriented design, especially where it comes to its extensions (which are known as cogs in  
Discord.py). Despite this, we still strive to provide an idiomatic API that makes full use of Kotlin's niceties.

If you're ready to get started, please [take a look at the documentation](https://kordex.kotlindiscord.com/).

# Why not kordx.commands?

Kord has released their own command framework, [kordx.commands](https://github.com/kordlib/kordx.commands). It's
a competent library, but it takes some very different approaches to solving the same problems Kord Extensions does.
Most  notably, it requires the use of [kapt](https://kotlinlang.org/docs/reference/kapt.html) and makes use of an
annotation-based autowire system for getting things registered.

In contrast, Kord Extensions provides a less magical approach that is more closely tied to object-oriented
programming, and may be more suitable for embedding into other applications. In addition, it provides many useful 
utilities and niceties that make working with Kord a breeze. At the end of the day, though, the
choice is yours - both approaches have pros and cons, and it's worth checking both out to see what you like
better!

# Under Development

This file is in an early state, and we're working on bringing over our framework from our bot project. Once we're
happy with what we've done, and we've written up some documentation, we'll update this file and make a proper
release.
