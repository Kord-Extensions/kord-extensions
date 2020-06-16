Kord Extensions
===============

Kord Extensions is an addon for the excellent [Kord library](https://github.com/kordlib/kord). It intends to provide
a framework for larger bot projects, with easy-to-use commands and event handling, wrapped up into individual
Extension objects.

The approach taken here is relatively different from a lot of Kotlin libraries, many of which prefer to provide a
DSL for quickly prototyping or implementing a small application. Instead, 
[Discord.py](https://github.com/Rapptz/discord.py) (the Discord library for Python) is a primary source of inspiration
for our fairly object-oriented design, especially where it comes to its extensions (which are known as cogs in 
Discord.py). Despite this, we still strive to provide an idiomatic API that makes full use of Kotlin's niceties.

**Note:** Kord is [already working on their own command framework](https://github.com/kordlib/kord/issues/8). This
is an independent library, created at a time when we couldn't wait for an official command framework. Once Kord
has released their framework, we'll evaluate the possibility of supporting it directly in ours (if necessary).

Under Development
=================

This file is in an early state, and we're working on bringing over our framework from our bot project. Once we're
happy with what we've done, and we've written up some documentation, we'll update this file and make a proper
release.
