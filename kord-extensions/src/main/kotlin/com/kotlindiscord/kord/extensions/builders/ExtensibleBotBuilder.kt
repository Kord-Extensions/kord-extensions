/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

@file:OptIn(PrivilegedIntent::class)

package com.kotlindiscord.kord.extensions.builders

import com.kotlindiscord.kord.extensions.DISCORD_BLURPLE
import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.annotations.BotBuilderDSL
import com.kotlindiscord.kord.extensions.checks.types.*
import com.kotlindiscord.kord.extensions.commands.application.ApplicationCommandRegistry
import com.kotlindiscord.kord.extensions.commands.application.DefaultApplicationCommandRegistry
import com.kotlindiscord.kord.extensions.commands.chat.ChatCommandRegistry
import com.kotlindiscord.kord.extensions.components.ComponentRegistry
import com.kotlindiscord.kord.extensions.components.callbacks.ComponentCallbackRegistry
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.i18n.ResourceBundleTranslations
import com.kotlindiscord.kord.extensions.i18n.SupportedLocales
import com.kotlindiscord.kord.extensions.i18n.TranslationsProvider
import com.kotlindiscord.kord.extensions.koin.KordExContext
import com.kotlindiscord.kord.extensions.plugins.KordExPlugin
import com.kotlindiscord.kord.extensions.plugins.PluginManager
import com.kotlindiscord.kord.extensions.sentry.SentryAdapter
import com.kotlindiscord.kord.extensions.storage.DataAdapter
import com.kotlindiscord.kord.extensions.storage.toml.TomlDataAdapter
import com.kotlindiscord.kord.extensions.types.FailureReason
import com.kotlindiscord.kord.extensions.utils.getKoin
import com.kotlindiscord.kord.extensions.utils.loadModule
import dev.kord.cache.api.DataCache
import dev.kord.common.Color
import dev.kord.common.entity.PresenceStatus
import dev.kord.common.entity.Snowflake
import dev.kord.core.ClientResources
import dev.kord.core.Kord
import dev.kord.core.behavior.GuildBehavior
import dev.kord.core.behavior.UserBehavior
import dev.kord.core.behavior.channel.ChannelBehavior
import dev.kord.core.builder.kord.KordBuilder
import dev.kord.core.cache.KordCacheBuilder
import dev.kord.core.entity.interaction.Interaction
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.supplier.EntitySupplier
import dev.kord.core.supplier.EntitySupplyStrategy
import dev.kord.gateway.Intent
import dev.kord.gateway.Intents
import dev.kord.gateway.PrivilegedIntent
import dev.kord.gateway.builder.PresenceBuilder
import dev.kord.gateway.builder.Shards
import dev.kord.rest.builder.message.create.MessageCreateBuilder
import dev.kord.rest.builder.message.create.allowedMentions
import io.ktor.utils.io.*
import mu.KLogger
import mu.KotlinLogging
import org.koin.core.logger.Level
import org.koin.dsl.bind
import org.koin.fileProperties
import org.koin.logger.slf4jLogger
import java.io.File
import java.nio.file.Path
import java.util.*
import kotlin.io.path.Path
import kotlin.io.path.div
import dev.kord.common.Locale as KLocale

internal typealias LocaleResolver = suspend (
    guild: GuildBehavior?,
    channel: ChannelBehavior?,
    user: UserBehavior?,
    interaction: Interaction?
) -> Locale?

internal typealias FailureResponseBuilder =
    suspend (MessageCreateBuilder).(message: String, type: FailureReason<*>) -> Unit

/**
 * Builder class used for configuring and creating an [ExtensibleBot].
 *
 * This is a one-stop-shop for pretty much everything you could possibly need to change to configure your bot, via
 * properties and a bunch of DSL functions.
 */
@BotBuilderDSL
public open class ExtensibleBotBuilder {
    protected val logger: KLogger = KotlinLogging.logger {}

    /** Called to create an [ExtensibleBot], can be set to the constructor of your own subtype if needed. **/
    public var constructor: (ExtensibleBotBuilder, String) -> ExtensibleBot = ::ExtensibleBot

    /** @suppress Builder that shouldn't be set directly by the user. **/
    public val cacheBuilder: CacheBuilder = CacheBuilder()

    /** @suppress Builder that shouldn't be set directly by the user. **/
    public val componentsBuilder: ComponentsBuilder = ComponentsBuilder()

    /** Data storage adapter to use for all extensions, modules and plugins. **/
    public var dataAdapterCallback: () -> DataAdapter<*> = ::TomlDataAdapter

    /**
     * @suppress Builder that shouldn't be set directly by the user.
     */
    public var failureResponseBuilder: FailureResponseBuilder = { message, _ ->
        allowedMentions { }

        content = message
    }

    /** @suppress Builder that shouldn't be set directly by the user. **/
    public open val extensionsBuilder: ExtensionsBuilder = ExtensionsBuilder()

    /** @suppress Used for late execution of extensions builder calls, so plugins can be loaded first. **/
    protected open val deferredExtensionsBuilders: MutableList<suspend ExtensionsBuilder.() -> Unit> =
        mutableListOf()

    /** @suppress Builder that shouldn't be set directly by the user. **/
    public val hooksBuilder: HooksBuilder = HooksBuilder()

    /** @suppress Builder that shouldn't be set directly by the user. **/
    public val i18nBuilder: I18nBuilder = I18nBuilder()

    /** @suppress Plugin builder. **/
    public val pluginBuilder: PluginBuilder = PluginBuilder(this)

    /** @suppress Builder that shouldn't be set directly by the user. **/
    public var intentsBuilder: (Intents.IntentsBuilder.() -> Unit)? = {
        +Intents.nonPrivileged

        if (chatCommandsBuilder.enabled) {
            +Intent.MessageContent
        }

        getKoin().get<ExtensibleBot>().extensions.values.forEach { extension ->
            extension.intents.forEach {
                +it
            }
        }
    }

    /** @suppress Builder that shouldn't be set directly by the user. **/
    public val membersBuilder: MembersBuilder = MembersBuilder()

    /** @suppress Builder that shouldn't be set directly by the user. **/
    public val chatCommandsBuilder: ChatCommandsBuilder = ChatCommandsBuilder()

    /** @suppress Builder that shouldn't be set directly by the user. **/
    public var presenceBuilder: PresenceBuilder.() -> Unit = { status = PresenceStatus.Online }

    /** @suppress Builder that shouldn't be set directly by the user. **/
    public var shardingBuilder: ((recommended: Int) -> Shards)? = null

    /** @suppress Builder that shouldn't be set directly by the user. **/
    public val applicationCommandsBuilder: ApplicationCommandsBuilder = ApplicationCommandsBuilder()

    /** @suppress List of Kord builders, shouldn't be set directly by the user. **/
    public val kordHooks: MutableList<suspend KordBuilder.() -> Unit> = mutableListOf()

    /** @suppress Kord builder, creates a Kord instance. **/
    public var kordBuilder: suspend (String, suspend KordBuilder.() -> Unit) -> Kord = { token, builder ->
        Kord(token) { builder() }
    }

    /** Logging level Koin should use, defaulting to ERROR. **/
    public var koinLogLevel: Level = Level.ERROR

    /**
     * DSL function used to configure the bot's caching options.
     *
     * @see CacheBuilder
     */
    @BotBuilderDSL
    public suspend fun cache(builder: suspend CacheBuilder.() -> Unit) {
        builder(cacheBuilder)
    }

    /**
     * Call this to register a custom data adapter class. Generally you'd pass a constructor here, but you can
     * also provide a lambda if needed.
     */
    @BotBuilderDSL
    public fun dataAdapter(builder: () -> DataAdapter<*>) {
        dataAdapterCallback = builder
    }

    /**
     * DSL function used to configure the bot's plugin loading options.
     *
     * @see PluginBuilder
     */
    @BotBuilderDSL
    public suspend fun plugins(builder: suspend PluginBuilder.() -> Unit) {
        builder(pluginBuilder)
    }

    /**
     * DSL function used to configure the bot's components system.
     *
     * @see ComponentsBuilder
     */
    @BotBuilderDSL
    public suspend fun components(builder: suspend ComponentsBuilder.() -> Unit) {
        builder(componentsBuilder)
    }

    /**
     * Register the message builder responsible for formatting error responses, which are sent to users during command
     * and component body execution.
     */
    @BotBuilderDSL
    public fun errorResponse(builder: FailureResponseBuilder) {
        failureResponseBuilder = builder
    }

    /**
     * DSL function used to insert code at various points in the bot's lifecycle.
     *
     * @see HooksBuilder
     */
    @BotBuilderDSL
    public suspend fun hooks(builder: suspend HooksBuilder.() -> Unit) {
        builder(hooksBuilder)
    }

    /**
     * DSL function allowing for additional Kord configuration builders to be specified, allowing for direct
     * customisation of the Kord object.
     *
     * Multiple builders may be registered, and they'll be called in the order they were registered here. Builders are
     * called after Kord Extensions has applied its own builder actions - so you can override the changes it makes here
     * if they don't suit your bot.
     *
     * @see KordBuilder
     */
    @BotBuilderDSL
    public fun kord(builder: suspend KordBuilder.() -> Unit) {
        kordHooks.add(builder)
    }

    /**
     * Function allowing you to specify a callable that constructs and returns a Kord instance. This can be used
     * to specify your own Kord subclass, if you need to - but shouldn't be a replacement for registering a [kord]
     * configuration builder.
     *
     * @see Kord
     */
    @BotBuilderDSL
    public fun customKordBuilder(builder: suspend (String, suspend KordBuilder.() -> Unit) -> Kord) {
        kordBuilder = builder
    }

    /**
     * DSL function used to configure the bot's chat command options.
     *
     * @see ChatCommandsBuilder
     */
    @BotBuilderDSL
    public suspend fun chatCommands(builder: suspend ChatCommandsBuilder.() -> Unit) {
        builder(chatCommandsBuilder)
    }

    /**
     * DSL function used to configure the bot's application command options.
     *
     * @see ApplicationCommandsBuilder
     */
    @BotBuilderDSL
    public suspend fun applicationCommands(builder: suspend ApplicationCommandsBuilder.() -> Unit) {
        builder(applicationCommandsBuilder)
    }

    /**
     * DSL function used to configure the bot's extension options, and add extensions. Calls to this function **do not
     * run immediately**, so that plugins can be loaded beforehand.
     *
     * @see ExtensionsBuilder
     */
    @BotBuilderDSL
    public open suspend fun extensions(builder: suspend ExtensionsBuilder.() -> Unit) {
        deferredExtensionsBuilders.add(builder)
    }

    /**
     * DSL function used to configure the bot's intents.
     *
     * @param addDefaultIntents Whether to automatically add all non-privileged intents to the builder before running
     * the given lambda.
     * @param addDefaultIntents Whether to automatically add the required intents defined within each loaded extension
     *
     * @see Intents.IntentsBuilder
     */
    @BotBuilderDSL
    public fun intents(
        addDefaultIntents: Boolean = true,
        addExtensionIntents: Boolean = true,
        builder: Intents.IntentsBuilder.() -> Unit
    ) {
        this.intentsBuilder = {
            if (addDefaultIntents) {
                +Intents.nonPrivileged

                if (chatCommandsBuilder.enabled) {
                    +Intent.MessageContent
                }
            }

            if (addExtensionIntents) {
                getKoin().get<ExtensibleBot>().extensions.values.forEach { extension ->
                    extension.intents.forEach {
                        +it
                    }
                }
            }

            builder()
        }
    }

    /**
     * DSL function used to configure the bot's i18n settings.
     *
     * @see I18nBuilder
     */
    @BotBuilderDSL
    public suspend fun i18n(builder: suspend I18nBuilder.() -> Unit) {
        builder(i18nBuilder)
    }

    /**
     * DSL function used to configure the bot's member-related options.
     *
     * @see MembersBuilder
     */
    @BotBuilderDSL
    public suspend fun members(builder: suspend MembersBuilder.() -> Unit) {
        builder(membersBuilder)
    }

    /**
     * DSL function used to configure the bot's initial presence.
     *
     * @see PresenceBuilder
     */
    @BotBuilderDSL
    public fun presence(builder: PresenceBuilder.() -> Unit) {
        this.presenceBuilder = builder
    }

    /**
     * DSL function used to configure the bot's sharding settings.
     *
     * @see dev.kord.core.builder.kord.KordBuilder.shardsBuilder
     */
    @BotBuilderDSL
    public fun sharding(shards: (recommended: Int) -> Shards) {
        this.shardingBuilder = shards
    }

    /** @suppress Internal function used to initially set up Koin. **/
    public open suspend fun setupKoin() {
        startKoinIfNeeded()

        hooksBuilder.runBeforeKoinSetup()

        addBotKoinModules()

        hooksBuilder.runAfterKoinSetup()
    }

    /** @suppress Creates a new KoinApplication if it has not already been started. **/
    private fun startKoinIfNeeded() {
        var logLevel = koinLogLevel

        if (logLevel == Level.INFO || logLevel == Level.DEBUG) {
            // NOTE: Temporary workaround for Koin not supporting Kotlin 1.6
            logLevel = Level.ERROR
        }

        if (koinNotStarted()) {
            KordExContext.startKoin {
                slf4jLogger(logLevel)
//                environmentProperties()  // https://github.com/InsertKoinIO/koin/issues/1099

                if (File("koin.properties").exists()) {
                    fileProperties("koin.properties")
                }
            }
        } else {
            getKoin().logger.level = logLevel
        }
    }

    /** @suppress Internal function that checks if Koin has been started. **/
    private fun koinNotStarted(): Boolean = KordExContext.getOrNull() == null

    /**
     * @suppress Internal function that creates and loads the bot's main Koin modules.
     * The modules provide important bot-related singletons.
     **/
    private fun addBotKoinModules() {
        loadModule { single { this@ExtensibleBotBuilder } bind ExtensibleBotBuilder::class }
        loadModule { single { i18nBuilder.translationsProvider } bind TranslationsProvider::class }
        loadModule { single { chatCommandsBuilder.registryBuilder() } bind ChatCommandRegistry::class }
        loadModule { single { componentsBuilder.registryBuilder() } bind ComponentRegistry::class }
        loadModule { single { componentsBuilder.callbackRegistryBuilder() } bind ComponentCallbackRegistry::class }

        loadModule {
            single {
                applicationCommandsBuilder.applicationCommandRegistryBuilder()
            } bind ApplicationCommandRegistry::class
        }

        loadModule {
            single {
                val adapter = extensionsBuilder.sentryExtensionBuilder.builder()

                if (extensionsBuilder.sentryExtensionBuilder.enable) {
                    extensionsBuilder.sentryExtensionBuilder.setupCallback(adapter)
                }

                adapter
            } bind SentryAdapter::class
        }
    }

    /** @suppress Plugin-loading function. **/
    @Suppress("TooGenericExceptionCaught")
    public open suspend fun loadPlugins() {
        val manager = pluginBuilder.manager(pluginBuilder.pluginPaths)

        loadModule { single { manager } bind PluginManager::class }

        pluginBuilder.managerObj = manager
        pluginBuilder.disabledPlugins.forEach(manager::disablePlugin)

        manager.loadPlugins()

        manager.plugins.forEach { wrapper ->
            val plugin = wrapper.plugin as? KordExPlugin

            plugin?.settingsCallbacks?.forEach { callback ->
                try {
                    callback(this)
                } catch (t: Throwable) {
                    logger.error(t) { "Error thrown while running settings callbacks for plugin: ${wrapper.pluginId}" }
                }
            }
        }
    }

    /** @suppress Plugin-loading function. **/
    public open suspend fun startPlugins() {
        pluginBuilder.managerObj.startPlugins()
    }

    /** @suppress Internal function used to build a bot instance. **/
    public open suspend fun build(token: String): ExtensibleBot {
        hooksBuilder.beforeKoinSetup {  // We have to do this super-duper early for safety
            loadModule { single { dataAdapterCallback() } bind DataAdapter::class }
        }

        hooksBuilder.beforeKoinSetup {
            if (pluginBuilder.enabled) {
                loadPlugins()
            }

            deferredExtensionsBuilders.forEach { it(extensionsBuilder) }
        }

        setupKoin()

        val bot = constructor(this, token)

        loadModule { single { bot } bind ExtensibleBot::class }

        hooksBuilder.runCreated(bot)

        bot.setup()

        hooksBuilder.runSetup(bot)
        hooksBuilder.runBeforeExtensionsAdded(bot)

        @Suppress("TooGenericExceptionCaught")
        extensionsBuilder.extensions.forEach {
            try {
                bot.addExtension(it)
            } catch (e: Exception) {
                logger.error(e) {
                    "Failed to set up extension: $it"
                }
            }
        }

        if (pluginBuilder.enabled) {
            startPlugins()
        }

        hooksBuilder.runAfterExtensionsAdded(bot)

        return bot
    }

    /**
     * Builder used for configuring the bot's wired-plugin-loading options.
     *
     * @property parent Parent [ExtensibleBotBuilder], for extension functions.
     */
    @BotBuilderDSL
    public class PluginBuilder(public val parent: ExtensibleBotBuilder) {
        internal lateinit var managerObj: PluginManager

        /** Whether to attempt to load wired plugin. Defaults to `true`. **/
        public var enabled: Boolean = true

        /** Plugin manager builder, which you can replace if your needs require it. **/
        public var manager: (List<Path>) -> PluginManager = ::PluginManager

        /** List of paths to load plugin from. Uses `plugins/` in the current working directory by default. **/
        public val pluginPaths: MutableList<Path> = mutableListOf(
            Path(".") / "plugins"
        )

        /** List of plugin IDs to disable. Plugins in this list will not be loaded automatically. **/
        public val disabledPlugins: MutableList<String> = mutableListOf()

        /**
         * Convenience function for disabling a plugin by ID.
         *
         * @see disabledPlugins
         */
        public fun disable(id: String) {
            disabledPlugins.add(id)
        }

        /**
         * Convenience function for adding a plugin path.
         *
         * @see pluginPaths
         */
        public fun pluginPath(path: String) {
            pluginPaths.add(Path.of(path))
        }

        /**
         * Convenience function for adding a plugin path.
         *
         * @see pluginPaths
         */
        public fun pluginPath(path: Path) {
            pluginPaths.add(path)
        }
    }

    /** Builder used for configuring the bot's caching options. **/
    @BotBuilderDSL
    public class CacheBuilder {
        /**
         * Number of messages to keep in the cache. Defaults to 10,000.
         *
         * To disable automatic configuration of the message cache, set this to `null` or `0`. You can configure the
         * cache yourself using the [kord] function, and interact with the resulting [DataCache] object using the
         * [transformCache] function.
         */
        @Suppress("MagicNumber")
        public var cachedMessages: Int? = 10_000

        /** The default Kord caching strategy - defaults to caching REST when an entity doesn't exist in the cache. **/
        public var defaultStrategy: EntitySupplyStrategy<EntitySupplier> =
            EntitySupplyStrategy.cacheWithCachingRestFallback

        /** @suppress Builder that shouldn't be set directly by the user. **/
        public var builder: (KordCacheBuilder.(resources: ClientResources) -> Unit) = {
            if (cachedMessages != null && cachedMessages!! > 0) {
                messages(lruCache(cachedMessages!!))
            }
        }

        /** @suppress Builder that shouldn't be set directly by the user. **/
        public var dataCacheBuilder: suspend Kord.(cache: DataCache) -> Unit = {}

        /** DSL function allowing you to customize Kord's cache. **/
        public fun kord(builder: KordCacheBuilder.(resources: ClientResources) -> Unit) {
            this.builder = {
                if (cachedMessages != null && cachedMessages!! > 0) {
                    messages(lruCache(cachedMessages!!))
                }

                builder.invoke(this, it)
            }
        }

        /** DSL function allowing you to interact with Kord's [DataCache] before it connects to Discord. **/
        public fun transformCache(builder: suspend Kord.(cache: DataCache) -> Unit) {
            this.dataCacheBuilder = builder
        }
    }

    /** Builder used to configure the bot's components settings. **/
    @BotBuilderDSL
    public class ComponentsBuilder {
        /** @suppress Component callback registry builder. **/
        public var callbackRegistryBuilder: () -> ComponentCallbackRegistry = ::ComponentCallbackRegistry

        /** @suppress Component registry builder. **/
        public var registryBuilder: () -> ComponentRegistry = ::ComponentRegistry

        /**
         * Register a builder (usually a constructor) returning a [ComponentCallbackRegistry] instance, which may
         * be useful if you need to register a custom subclass.
         */
        public fun callbackRegistry(builder: () -> ComponentCallbackRegistry) {
            callbackRegistryBuilder = builder
        }

        /**
         * Register a builder (usually a constructor) returning a [ComponentRegistry] instance, which may be useful
         * if you need to register a custom subclass.
         */
        public fun registry(builder: () -> ComponentRegistry) {
            registryBuilder = builder
        }
    }

    /** Builder used for configuring the bot's extension options, and registering custom extensions. **/
    @BotBuilderDSL
    public open class ExtensionsBuilder {
        /** @suppress Internal list that shouldn't be modified by the user directly. **/
        public open val extensions: MutableList<() -> Extension> = mutableListOf()

        /** @suppress Help extension builder. **/
        public open val helpExtensionBuilder: HelpExtensionBuilder = HelpExtensionBuilder()

        /** @suppress Sentry extension builder. **/
        public open val sentryExtensionBuilder: SentryExtensionBuilder = SentryExtensionBuilder()

        /** Add a custom extension to the bot via a builder - probably the extension constructor. **/
        public open fun add(builder: () -> Extension) {
            extensions.add(builder)
        }

        /** Configure the built-in help extension, or disable it so you can use your own. **/
        public open suspend fun help(builder: HelpExtensionBuilder.() -> Unit) {
            builder(helpExtensionBuilder)
        }

        /** Configure the built-in sentry extension, or disable it so you can use your own. **/
        public open suspend fun sentry(builder: SentryExtensionBuilder.() -> Unit) {
            builder(sentryExtensionBuilder)
        }

        /** Builder used to configure Sentry and the Sentry extension. **/
        @BotBuilderDSL
        public open class SentryExtensionBuilder {
            /** Whether to enable Sentry integration. This includes the extension, and [SentryAdapter] setup. **/
            public open var enable: Boolean = false

            /**
             * Whether to enable the Sentry extension, which provides feedback commands.
             *
             * This will be ignored if [enable] is `false`.
             */
            public open var feedbackExtension: Boolean = false

            /** Whether to enable Sentry's debug mode. **/
            public open var debug: Boolean = false

            /** Your Sentry DSN, required for submitting events to Sentry. **/
            public open var dsn: String? = null

            /** Optional distribution name to send to Sentry. **/
            public open var distribution: String? = null

            /** Optional environment name to send to Sentry. **/
            public open var environment: String? = null

            /** Optional release version to send to Sentry. **/
            public open var release: String? = null

            /** Optional server name to send to Sentry. **/
            public open var serverName: String? = null

            /** Whether to ping users when responding to them. **/
            public var pingInReply: Boolean = true

            /** Builder used to construct a [SentryAdapter] instance, usually the constructor. **/
            public open var builder: () -> SentryAdapter = ::SentryAdapter

            /**
             * Function in charge of setting up the [SentryAdapter], by calling its `setup` function. You can use this
             * if you need to pass extra parameters to the setup function, but make sure you pass everything that's
             * required.
             */
            public open var setupCallback: SentryAdapter.() -> Unit = {
                this.setup(
                    dsn = dsn,
                    debug = debug,

                    distribution = distribution,
                    environment = environment,
                    release = release,
                    serverName = serverName,
                )
            }

            /** Register a builder used to construct a [SentryAdapter] instance, usually the constructor. **/
            public fun builder(body: () -> SentryAdapter) {
                builder = body
            }

            /**
             * Convenience function to enable and set the DSN, but only if the supplied DSN isn't null.
             *
             * Intended for use with `envOrNull`.
             */
            public fun enableIfDSN(sentryDSN: String?) {
                if (sentryDSN != null) {
                    dsn = sentryDSN
                    enable = true
                }
            }

            /**
             * Register the function in charge of setting up the [SentryAdapter], by calling its `setup` function.
             * You can use this if you need to pass extra parameters to the setup function, but make sure you pass
             * everything that's required.
             */
            public fun setup(body: SentryAdapter.() -> Unit) {
                setupCallback = body
            }
        }

        /** Builder used for configuring options, specifically related to the help extension. **/
        @BotBuilderDSL
        public open class HelpExtensionBuilder {
            /** Whether to enable the bundled help extension. Defaults to `true`. **/
            public var enableBundledExtension: Boolean = true

            /**
             * Time to wait before the help paginator times out and can't be used, in seconds. Defaults to 60.
             */
            @Suppress("MagicNumber")
            public var paginatorTimeout: Long = 60L  // 60 seconds

            /** Whether to delete the help paginator after the timeout ends. **/
            public var deletePaginatorOnTimeout: Boolean = false

            /** Whether to delete the help command invocation after the paginator timeout ends. **/
            public var deleteInvocationOnPaginatorTimeout: Boolean = false

            /** Whether to ping users when responding to them. **/
            public var pingInReply: Boolean = true

            /** List of command checks. These checks will be checked against all commands in the help extension. **/
            public val checkList: MutableList<ChatCommandCheck> = mutableListOf()

            /** For custom help embed colours. Only one may be defined. **/
            public var colourGetter: suspend MessageCreateEvent.() -> Color = { DISCORD_BLURPLE }

            /** Define a callback that returns a [Color] to use for help embed colours. Feel free to mix it up! **/
            public fun colour(builder: suspend MessageCreateEvent.() -> Color) {
                colourGetter = builder
            }

            /** Like [colour], but American. **/
            public fun color(builder: suspend MessageCreateEvent.() -> Color): Unit = colour(builder)

            /**
             * Define a check which must pass for help commands to be executed. This check will be applied to all
             * commands in the extension.
             *
             * A command may have multiple checks - all checks must pass for the command to be executed.
             * Checks will be run in the order that they're defined.
             *
             * This function can be used DSL-style with a given body, or it can be passed one or more
             * predefined functions. See the samples for more information.
             *
             * @param checks Checks to apply to all help commands.
             */
            public fun check(vararg checks: ChatCommandCheck) {
                checks.forEach { checkList.add(it) }
            }

            /**
             * Overloaded check function to allow for DSL syntax.
             *
             * @param check Check to apply to all help commands.
             */
            public fun check(check: ChatCommandCheck) {
                checkList.add(check)
            }
        }
    }

    /** Builder used to insert code at various points in the bot's lifecycle. **/
    @Suppress("TooGenericExceptionCaught")
    // We need to catch literally everything in here
    @BotBuilderDSL
    public class HooksBuilder {
        // region: Hook lists

        /**
         * Whether Kord's shutdown hook should be registered. When enabled, Kord logs out of the gateway on shutdown.
         */
        public var kordShutdownHook: Boolean = true

        /** @suppress Internal list of hooks. **/
        public val afterExtensionsAddedList: MutableList<suspend ExtensibleBot.() -> Unit> = mutableListOf()

        /** @suppress Internal list of hooks. **/
        public val afterKoinSetupList: MutableList<suspend () -> Unit> = mutableListOf()

        /** @suppress Internal list of hooks. **/
        public val beforeKoinSetupList: MutableList<suspend () -> Unit> = mutableListOf()

        /** @suppress Internal list of hooks. **/
        public val beforeExtensionsAddedList: MutableList<suspend ExtensibleBot.() -> Unit> = mutableListOf()

        /** @suppress Internal list of hooks. **/
        public val beforeStartList: MutableList<suspend ExtensibleBot.() -> Unit> = mutableListOf()

        /** @suppress Internal list of hooks. **/
        public val createdList: MutableList<suspend ExtensibleBot.() -> Unit> = mutableListOf()

        /** @suppress Internal list of hooks. **/
        public val extensionAddedList: MutableList<suspend ExtensibleBot.(extension: Extension) -> Unit> =
            mutableListOf()

        /** @suppress Internal list of hooks. **/
        public val setupList: MutableList<suspend ExtensibleBot.() -> Unit> = mutableListOf()

        // endregion

        // region DSL functions

        /**
         * Register a lambda to be called after all the extensions in the [ExtensionsBuilder] have been added. This
         * will be called regardless of how many were successfully set up.
         */
        @BotBuilderDSL
        public fun afterExtensionsAdded(body: suspend ExtensibleBot.() -> Unit): Boolean =
            afterExtensionsAddedList.add(body)

        /**
         * Register a lambda to be called after Koin has been set up. You can use this to register overriding modules
         * via `loadModule` before the modules are actually accessed.
         */
        @BotBuilderDSL
        public fun afterKoinSetup(body: suspend () -> Unit): Boolean =
            afterKoinSetupList.add(body)

        /**
         * Register a lambda to be called before Koin has been set up. You can use this to register Koin modules
         * early, if needed.
         */
        @BotBuilderDSL
        public fun beforeKoinSetup(body: suspend () -> Unit): Boolean =
            beforeKoinSetupList.add(body)

        /**
         * Register a lambda to be called before all the extensions in the [ExtensionsBuilder] have been added.
         */
        @BotBuilderDSL
        public fun beforeExtensionsAdded(body: suspend ExtensibleBot.() -> Unit): Boolean =
            beforeExtensionsAddedList.add(body)

        /**
         * Register a lambda to be called just before the bot tries to connect to Discord.
         */
        @BotBuilderDSL
        public fun beforeStart(body: suspend ExtensibleBot.() -> Unit): Boolean =
            beforeStartList.add(body)

        /**
         * Register a lambda to be called right after the [ExtensibleBot] object has been created, before it gets set
         * up.
         */
        @BotBuilderDSL
        public fun created(body: suspend ExtensibleBot.() -> Unit): Boolean =
            createdList.add(body)

        /**
         * Register a lambda to be called after any extension is successfully added to the bot.
         */
        @BotBuilderDSL
        public fun extensionAdded(body: suspend ExtensibleBot.(extension: Extension) -> Unit): Boolean =
            extensionAddedList.add(body)

        /**
         * Register a lambda to be called after the [ExtensibleBot] object has been created and set up.
         */
        @BotBuilderDSL
        public fun setup(body: suspend ExtensibleBot.() -> Unit): Boolean =
            setupList.add(body)

        // endregion

        // region Hook execution functions

        /** @suppress Internal hook execution function. **/
        public suspend fun runAfterExtensionsAdded(bot: ExtensibleBot): Unit =
            afterExtensionsAddedList.forEach {
                try {
                    it.invoke(bot)
                } catch (t: Throwable) {
                    bot.logger.error(t) {
                        "Failed to run extensionAdded hook $it"
                    }
                }
            }

        /** @suppress Internal hook execution function. **/
        public suspend fun runAfterKoinSetup() {
            val logger: KLogger = KotlinLogging.logger {}

            afterKoinSetupList.forEach {
                try {
                    it.invoke()
                } catch (t: Throwable) {
                    logger.error(t) {
                        "Failed to run afterKoinSetup hook $it"
                    }
                }
            }
        }

        /** @suppress Internal hook execution function. **/
        public suspend fun runBeforeKoinSetup() {
            val logger: KLogger = KotlinLogging.logger {}

            beforeKoinSetupList.forEach {
                try {
                    it.invoke()
                } catch (t: Throwable) {
                    logger.error(t) {
                        "Failed to run beforeKoinSetup hook $it"
                    }
                }
            }
        }

        /** @suppress Internal hook execution function. **/
        public suspend fun runBeforeExtensionsAdded(bot: ExtensibleBot): Unit =
            beforeExtensionsAddedList.forEach {
                try {
                    it.invoke(bot)
                } catch (t: Throwable) {
                    bot.logger.error(t) {
                        "Failed to run beforeExtensionsAdded hook $it"
                    }
                }
            }

        /** @suppress Internal hook execution function. **/
        public suspend fun runBeforeStart(bot: ExtensibleBot): Unit =
            beforeStartList.forEach {
                try {
                    it.invoke(bot)
                } catch (t: Throwable) {
                    bot.logger.error(t) {
                        "Failed to run beforeStart hook $it"
                    }
                }
            }

        /** @suppress Internal hook execution function. **/
        public suspend fun runCreated(bot: ExtensibleBot): Unit =
            createdList.forEach {
                try {
                    it.invoke(bot)
                } catch (t: Throwable) {
                    bot.logger.error(t) {
                        "Failed to run created hook $it"
                    }
                }
            }

        /** @suppress Internal hook execution function. **/
        public suspend fun runExtensionAdded(bot: ExtensibleBot, extension: Extension): Unit =
            extensionAddedList.forEach {
                try {
                    it.invoke(bot, extension)
                } catch (t: Throwable) {
                    bot.logger.error(t) {
                        "Failed to run extensionAdded hook $it"
                    }
                }
            }

        /** @suppress Internal hook execution function. **/
        public suspend fun runSetup(bot: ExtensibleBot): Unit =
            setupList.forEach {
                try {
                    it.invoke(bot)
                } catch (t: Throwable) {
                    bot.logger.error(t) {
                        "Failed to run setup hook $it"
                    }
                }
            }

        // endregion
    }

    /** Builder used to configure i18n options. **/
    @BotBuilderDSL
    public class I18nBuilder {
        /** Locale that should be used by default. **/
        public var defaultLocale: Locale = SupportedLocales.ENGLISH

        /**
         * List of [locales][KLocale] which are used for application command names (without [defaultLocale]).
         */
        public var applicationCommandLocales: MutableList<KLocale> = mutableListOf()

        /**
         * Callables used to resolve a Locale object for the given guild, channel and user.
         *
         * Resolves to [defaultLocale] by default.
         */
        public var localeResolvers: MutableList<LocaleResolver> = mutableListOf()

        /** Object responsible for retrieving translations. Users should get this via Koin or other methods. **/
        internal var translationsProvider: TranslationsProvider = ResourceBundleTranslations { defaultLocale }

        /** Call this with a builder (usually the class constructor) to set the translations provider. **/
        public fun translationsProvider(builder: (() -> Locale) -> TranslationsProvider) {
            translationsProvider = builder { defaultLocale }
        }

        /** Register a locale resolver, returning the required [Locale] object or `null`. **/
        public fun localeResolver(body: LocaleResolver) {
            localeResolvers.add(body)
        }

        /**
         * Registers [locale] as an application command language.
         *
         * **Do not register [defaultLocale]**
         */
        public fun applicationCommandLocale(vararg locale: KLocale) {
            applicationCommandLocales.addAll(locale.toList())
        }

        /**
         * Registers a [LocaleResolver] using [Interaction.locale].
         */
        public fun interactionUserLocaleResolver(): Unit =
            localeResolver { _, _, _, interaction ->
                interaction?.locale?.asJavaLocale()
            }

        /**
         * Registers a [LocaleResolver] using [Interaction.guildLocale].
         */
        public fun interactionGuildLocaleResolver(): Unit =
            localeResolver { _, _, _, interaction ->
                interaction?.guildLocale?.asJavaLocale()
            }
    }

    /** Builder used for configuring the bot's member-related options. **/
    @BotBuilderDSL
    public class MembersBuilder {
        /** @suppress Internal list that shouldn't be modified by the user directly. **/
        public var guildsToFill: MutableList<Snowflake>? = mutableListOf()

        /**
         * Whether to request the presences for the members that are requested from the guilds specified using the
         * functions in this class.
         *
         * Requires the `GUILD_PRESENCES` privileged intent. Make sure you've enabled it for your bot!
         */
        public var fillPresences: Boolean? = null

        /**
         * Whether to lock when requesting members from guilds, preventing concurrent requests from being processed
         * at once. This will slow down filling the cache with members, but may avoid hitting rate limits for larger
         * bots.
         */
        public var lockMemberRequests: Boolean = false

        /**
         * Add a list of guild IDs to request members for.
         *
         * Requires the `GUILD_MEMBERS` privileged intent. Make sure you've enabled it for your bot!
         */
        @JvmName("fillSnowflakes")  // These are the same for the JVM
        public fun fill(ids: Collection<Snowflake>): Boolean? =
            guildsToFill?.addAll(ids)

        /**
         * Add a list of guild IDs to request members for.
         *
         * Requires the `GUILD_MEMBERS` privileged intent. Make sure you've enabled it for your bot!
         */
        @JvmName("fillLongs")  // These are the same for the JVM
        public fun fill(ids: Collection<ULong>): Boolean? =
            guildsToFill?.addAll(ids.map { Snowflake(it) })

        /**
         * Add a list of guild IDs to request members for.
         *
         * Requires the `GUILD_MEMBERS` privileged intent. Make sure you've enabled it for your bot!
         */
        @JvmName("fillStrings")  // These are the same for the JVM
        public fun fill(ids: Collection<String>): Boolean? =
            guildsToFill?.addAll(ids.map { Snowflake(it) })

        /**
         * Add a guild ID to request members for.
         *
         * Requires the `GUILD_MEMBERS` privileged intent. Make sure you've enabled it for your bot!
         */
        public fun fill(id: Snowflake): Boolean? =
            guildsToFill?.add(id)

        /**
         * Add a guild ID to request members for.
         *
         * Requires the `GUILD_MEMBERS` privileged intent. Make sure you've enabled it for your bot!
         */
        public fun fill(id: ULong): Boolean? =
            guildsToFill?.add(Snowflake(id))

        /**
         * Add a guild ID to request members for.
         *
         * Requires the `GUILD_MEMBERS` privileged intent. Make sure you've enabled it for your bot!
         */
        public fun fill(id: String): Boolean? =
            guildsToFill?.add(Snowflake(id))

        /**
         * Request members for all guilds the bot is on.
         *
         * Requires the `GUILD_MEMBERS` privileged intent. Make sure you've enabled it for your bot!
         */
        public fun all() {
            guildsToFill = null
        }

        /**
         * Request no members from guilds at all. This is the default behaviour.
         */
        public fun none() {
            guildsToFill = mutableListOf()
        }
    }

    /** Builder used for configuring the bot's chat command options. **/
    @BotBuilderDSL
    public class ChatCommandsBuilder {
        /** Whether to invoke commands on bot mentions, in addition to using chat prefixes. Defaults to `true`. **/
        public var invokeOnMention: Boolean = true

        /** Prefix to require for command invocations on Discord. Defaults to `"!"`. **/
        public var defaultPrefix: String = "!"

        /** Whether to register and process chat commands. Defaults to `false`. **/
        public var enabled: Boolean = false

        /** @suppress Builder that shouldn't be set directly by the user. **/
        public var prefixCallback: suspend (MessageCreateEvent).(String) -> String = { defaultPrefix }

        /** @suppress Builder that shouldn't be set directly by the user. **/
        public var registryBuilder: () -> ChatCommandRegistry = { ChatCommandRegistry() }

        /**
         * List of command checks.
         *
         * These checks will be checked against all commands.
         */
        public val checkList: MutableList<ChatCommandCheck> = mutableListOf()

        /**
         * Register a lambda that takes a [MessageCreateEvent] object and the default prefix, and returns the
         * command prefix to be made use of for that message event.
         *
         * This is intended to allow for different chat command prefixes in different contexts - for example,
         * guild-specific prefixes.
         */
        public fun prefix(builder: suspend (MessageCreateEvent).(String) -> String) {
            prefixCallback = builder
        }

        /**
         * Register the builder used to create the [ChatCommandRegistry]. You can change this if you need to
         * make use of a subclass.
         */
        public fun registry(builder: () -> ChatCommandRegistry) {
            registryBuilder = builder
        }

        /**
         * Define a check which must pass for the command to be executed. This check will be applied to all commands.
         *
         * A command may have multiple checks - all checks must pass for the command to be executed.
         * Checks will be run in the order that they're defined.
         *
         * This function can be used DSL-style with a given body, or it can be passed one or more
         * predefined functions. See the samples for more information.
         *
         * @param checks Checks to apply to all commands.
         */
        public fun check(vararg checks: ChatCommandCheck) {
            checks.forEach { checkList.add(it) }
        }

        /**
         * Overloaded check function to allow for DSL syntax.
         *
         * @param check Checks to apply to all commands.
         */
        public fun check(check: ChatCommandCheck) {
            checkList.add(check)
        }
    }

    /** Builder used for configuring the bot's application command options. **/
    @BotBuilderDSL
    public class ApplicationCommandsBuilder {
        /** Whether to register and process application commands. Defaults to `true`. **/
        public var enabled: Boolean = true

        /** The guild ID to use for all global application commands. Intended for testing. **/
        public var defaultGuild: Snowflake? = null

        /** Whether to attempt to register the bot's application commands. Intended for multi-instance sharded bots. **/
        public var register: Boolean = true

        /**
         * Whether to sync application command permissions. KordEx will still enforce them if this is disabled, but
         * Discord won't be made aware of command restrictions based on users or roles.
         *
         * You may want to disable this if your bots service communities that have Discord's newest slash command
         * permissions experiment enabled.
         */
        public var syncPermissions: Boolean = true

        /** @suppress Builder that shouldn't be set directly by the user. **/
        public var applicationCommandRegistryBuilder: () -> ApplicationCommandRegistry =
            { DefaultApplicationCommandRegistry() }

        /**
         * List of message command checks.
         *
         * These checks will be checked against all message commands.
         */
        public val messageCommandChecks: MutableList<MessageCommandCheck> = mutableListOf()

        /**
         * List of slash command checks.
         *
         * These checks will be checked against all slash commands.
         */
        public val slashCommandChecks: MutableList<SlashCommandCheck> = mutableListOf()

        /**
         * List of user command checks.
         *
         * These checks will be checked against all user commands.
         */
        public val userCommandChecks: MutableList<UserCommandCheck> = mutableListOf()

        /** Set a guild ID to use for all global application commands. Intended for testing. **/
        public fun defaultGuild(id: Snowflake?) {
            defaultGuild = id
        }

        /** Set a guild ID to use for all global application commands. Intended for testing. **/
        public fun defaultGuild(id: ULong?) {
            defaultGuild = id?.let { Snowflake(it) }
        }

        /** Set a guild ID to use for all global application commands. Intended for testing. **/
        public fun defaultGuild(id: String?) {
            defaultGuild = id?.let { Snowflake(it) }
        }

        /**
         * Register the builder used to create the [ApplicationCommandRegistry]. You can change this if you need to make
         * use of a subclass.
         */
        public fun applicationCommandRegistry(builder: () -> ApplicationCommandRegistry) {
            applicationCommandRegistryBuilder = builder
        }

        /**
         * Define a check which must pass for a message command to be executed. This check will be applied to all
         * message commands.
         *
         * A message command may have multiple checks - all checks must pass for the command to be executed.
         * Checks will be run in the order that they're defined.
         *
         * This function can be used DSL-style with a given body, or it can be passed one or more
         * predefined functions. See the samples for more information.
         *
         * @param checks Checks to apply to all slash commands.
         */
        public fun messageCommandCheck(vararg checks: MessageCommandCheck) {
            checks.forEach { messageCommandChecks.add(it) }
        }

        /**
         * Overloaded message command check function to allow for DSL syntax.
         *
         * @param check Check to apply to all slash commands.
         */
        public fun messageCommandCheck(check: MessageCommandCheck) {
            messageCommandChecks.add(check)
        }

        /**
         * Define a check which must pass for a slash command to be executed. This check will be applied to all
         * slash commands.
         *
         * A slash command may have multiple checks - all checks must pass for the command to be executed.
         * Checks will be run in the order that they're defined.
         *
         * This function can be used DSL-style with a given body, or it can be passed one or more
         * predefined functions. See the samples for more information.
         *
         * @param checks Checks to apply to all slash commands.
         */
        public fun slashCommandCheck(vararg checks: SlashCommandCheck) {
            checks.forEach { slashCommandChecks.add(it) }
        }

        /**
         * Overloaded slash command check function to allow for DSL syntax.
         *
         * @param check Check to apply to all slash commands.
         */
        public fun slashCommandCheck(check: SlashCommandCheck) {
            slashCommandChecks.add(check)
        }

        /**
         * Define a check which must pass for a user command to be executed. This check will be applied to all
         * user commands.
         *
         * A user command may have multiple checks - all checks must pass for the command to be executed.
         * Checks will be run in the order that they're defined.
         *
         * This function can be used DSL-style with a given body, or it can be passed one or more
         * predefined functions. See the samples for more information.
         *
         * @param checks Checks to apply to all slash commands.
         */
        public fun userCommandCheck(vararg checks: UserCommandCheck) {
            checks.forEach { userCommandChecks.add(it) }
        }

        /**
         * Overloaded user command check function to allow for DSL syntax.
         *
         * @param check Check to apply to all slash commands.
         */
        public fun userCommandCheck(check: UserCommandCheck) {
            userCommandChecks.add(check)
        }
    }
}
