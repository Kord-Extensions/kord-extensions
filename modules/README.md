# Modules

This directory contains first-party KordEx modules, providing development tooling, utilities, or user-facing bot
functionality.

- `data/` - Modules for working with data, such as data adapters.
  - `data-mongodb` - MongoDB support module, providing a data adapter and libraries for working with MongoDB.
- `dev/` - Modules providing development tooling, utilities, or extra APIs.
  - `dev-java-time` - Duration converter that uses the Java Date/Time library, for bots that need to integrate with it.
  - `dev-time4j` - Duration converter that uses the Time4J library, for bots that need to integrate with it.
  - `dev-unsafe` - Provides "unsafe" variants of interaction-based types (such as slash commands) that allow you to handle
    the interaction yourself if you need to.
- `functionality/` - Modules providing user-facing bot functionality.
  - `func-mappings` - Minecraft mappings lookup extension, powered by Linkie Core.
    - **This has been [temporarily relocated](https://github.com/Kord-Extensions/temp-mappings) while we
      [wait for a contributor to respond](https://github.com/orgs/Kord-Extensions/discussions/3#discussioncomment-10318475)
      to our licensing discussion.**

      If you need this module immediately, stick with Kord Extensions version `1.9.0-SNAPSHOT`.
  - `func-phishing` - URL safety and anti-phishing extension, powered by the Sinking Yachts API.
  - `func-tags` - Basic tag/factoid management extension. For advanced tag management, see
    [kose kata](https://github.com/mazziechai/kose-kata).
  - `func-welcome` - Automatic welcome channel management extension, loading the settings from YAML files hosted
    online.
- `integrations/` - Modules adding integrations with other libraries and services.
  - `pluralkit` - Module and extension adding support for [PluralKit](https://pluralkit.me) to KordEx bots.
    Includes new PluralKit-based events that you can use in place of the usual message events.
- `web/` - Modules for working with the Kord Extensions web interface, including the core web module itself.
  - `web-core` - Core web interface module, adding both a frontend and backend.
    This module also provides a fistful of APIs allowing other modules to integrate with the web interface.
