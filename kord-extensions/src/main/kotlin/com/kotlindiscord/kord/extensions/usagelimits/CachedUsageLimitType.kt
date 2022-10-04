package com.kotlindiscord.kord.extensions.usagelimits

import com.kotlindiscord.kord.extensions.usagelimits.ratelimits.UsageHistory
import com.kotlindiscord.kord.extensions.usagelimits.ratelimits.UsageHistoryImpl
import dev.kord.common.entity.Snowflake

/** Local cache implementation for [UsageLimitType]. **/
public enum class CachedUsageLimitType : UsageLimitType {

    // a specific user can have a usageLimit
    COMMAND_USER {
        override fun getCooldown(context: DiscriminatingContext): Long {
            val commandId = context.event.command.hashCode().toString()
            val userId = context.user.id

            return userCommandCooldowns[userId to commandId] ?: 0
        }

        override fun setCooldown(context: DiscriminatingContext, until: Long) {
            val commandId = context.event.command.hashCode().toString()
            val userId = context.user.id

            userCommandCooldowns[userId to commandId] = until
        }

        override fun getUsageHistory(context: DiscriminatingContext): UsageHistory {
            val commandId = context.event.command.hashCode().toString()
            val userId = context.user.id

            return userCommandHistory[userId to commandId] ?: UsageHistoryImpl()
        }

        override fun setUsageHistory(context: DiscriminatingContext, usageHistory: UsageHistory) {
            val commandId = context.event.command.hashCode().toString()
            val userId = context.user.id

            userCommandHistory[userId to commandId] = usageHistory
        }
    },

    // a specific user can have a usageLimit in a specific channel for a command
    COMMAND_USER_CHANNEL {
        override fun getCooldown(context: DiscriminatingContext): Long {
            val commandId = context.event.command.hashCode().toString()
            val userId = context.user.id
            val channelId = context.channel.id

            return userChannelCommandCooldowns[Triple(userId, channelId, commandId)] ?: 0
        }

        override fun setCooldown(context: DiscriminatingContext, until: Long) {
            val commandId = context.event.command.hashCode().toString()
            val userId = context.user.id
            val channelId = context.channel.id

            userChannelCommandCooldowns[Triple(userId, channelId, commandId)] = until
        }

        override fun getUsageHistory(context: DiscriminatingContext): UsageHistory {
            val commandId = context.event.command.hashCode().toString()
            val userId = context.user.id
            val channelId = context.channel.id

            return userChannelCommandUsageHistory[Triple(userId, channelId, commandId)] ?: UsageHistoryImpl()
        }

        override fun setUsageHistory(context: DiscriminatingContext, usageHistory: UsageHistory) {
            val commandId = context.event.command.hashCode().toString()
            val userId = context.user.id
            val channelId = context.channel.id

            userChannelCommandUsageHistory[Triple(userId, channelId, commandId)] = usageHistory
        }
    },

    // user usageLimit in specific guild for a command
    COMMAND_USER_GUILD {
        override fun getCooldown(context: DiscriminatingContext): Long {
            val commandId = context.event.command.hashCode().toString()
            val userId = context.user.id
            val guildId = context.guildId ?: return 0

            return userGuildCommandCooldowns[Triple(userId, guildId, commandId)] ?: 0
        }

        override fun setCooldown(context: DiscriminatingContext, until: Long) {
            val commandId = context.event.command.hashCode().toString()
            val userId = context.user.id
            val guildId = context.guildId ?: return

            userGuildCommandCooldowns[Triple(userId, guildId, commandId)] = until
        }

        override fun getUsageHistory(context: DiscriminatingContext): UsageHistory {
            val commandId = context.event.command.hashCode().toString()
            val userId = context.user.id
            val guildId = context.guildId ?: return UsageHistoryImpl()

            return userGuildCommandUsageHistory[Triple(userId, guildId, commandId)] ?: UsageHistoryImpl()
        }

        override fun setUsageHistory(context: DiscriminatingContext, usageHistory: UsageHistory) {
            val commandId = context.event.command.hashCode().toString()
            val userId = context.user.id
            val guildId = context.guildId ?: return

            userGuildCommandUsageHistory[Triple(userId, guildId, commandId)] = usageHistory
        }
    },

    // a specific user can have a usageLimit across all commands
    GLOBAL_USER {
        override fun getCooldown(context: DiscriminatingContext): Long =
            userGlobalCooldowns[context.user.id] ?: 0

        override fun setCooldown(context: DiscriminatingContext, until: Long) {
            userGlobalCooldowns[context.user.id] = until
        }

        override fun getUsageHistory(context: DiscriminatingContext): UsageHistory =
            userGlobalHistory[context.user.id] ?: UsageHistoryImpl()

        override fun setUsageHistory(context: DiscriminatingContext, usageHistory: UsageHistory) {
            userGlobalHistory[context.user.id] = usageHistory
        }
    },

    // a specific user can have a usageLimit in a specific channel across all commands
    GLOBAL_USER_CHANNEL {
        override fun getCooldown(context: DiscriminatingContext): Long =
            userChannelCooldowns[context.user.id to context.channel.id] ?: 0

        override fun setCooldown(context: DiscriminatingContext, until: Long) {
            userChannelCooldowns[context.user.id to context.channel.id] = until
        }

        override fun getUsageHistory(context: DiscriminatingContext): UsageHistory =
            userChannelUsageHistory[context.user.id to context.channel.id] ?: UsageHistoryImpl()

        override fun setUsageHistory(context: DiscriminatingContext, usageHistory: UsageHistory) {
            userChannelUsageHistory[context.user.id to context.channel.id] = usageHistory
        }
    },

    // user usageLimit in specific guild across all commands
    GLOBAL_USER_GUILD {
        override fun getCooldown(context: DiscriminatingContext): Long {
            val guildId = context.guildId ?: return 0
            return userGuildCooldowns[context.user.id to guildId] ?: 0
        }

        override fun setCooldown(context: DiscriminatingContext, until: Long) {
            val guildId = context.guildId ?: return
            userGuildCooldowns[context.user.id to guildId] = until
        }

        override fun getUsageHistory(context: DiscriminatingContext): UsageHistory {
            val guildId = context.guildId ?: return UsageHistoryImpl()
            return userGuildUsageHistory[context.user.id to guildId] ?: UsageHistoryImpl()
        }

        override fun setUsageHistory(context: DiscriminatingContext, usageHistory: UsageHistory) {
            val guildId = context.guildId ?: return
            userGuildUsageHistory[context.user.id to guildId] = usageHistory
        }
    },

    // a usageLimit across all commands :thonk: (don't use this lol)
    GLOBAL {
        override fun getCooldown(context: DiscriminatingContext): Long = globalCooldown

        override fun setCooldown(context: DiscriminatingContext, until: Long) {
            globalCooldown = until
        }

        override fun getUsageHistory(context: DiscriminatingContext): UsageHistory = globalHistory

        override fun setUsageHistory(context: DiscriminatingContext, usageHistory: UsageHistory) {
            globalHistory = usageHistory
        }
    },

    // a usageLimit for a specific channel across all commands
    GLOBAL_CHANNEL {
        override fun getCooldown(context: DiscriminatingContext): Long = channelCooldowns[context.channel.id] ?: 0

        override fun setCooldown(context: DiscriminatingContext, until: Long) {
            channelCooldowns[context.channel.id] = until
        }

        override fun getUsageHistory(context: DiscriminatingContext): UsageHistory =
            channelUsageHistory[context.channel.id] ?: UsageHistoryImpl()

        override fun setUsageHistory(context: DiscriminatingContext, usageHistory: UsageHistory) {
            channelUsageHistory[context.channel.id] = usageHistory
        }
    },

    // a usageLimit for a guild across all commands
    GLOBAL_GUILD {
        override fun getCooldown(context: DiscriminatingContext): Long {
            val guildId = context.guildId ?: return 0
            return guildCooldowns[guildId] ?: 0
        }

        override fun setCooldown(context: DiscriminatingContext, until: Long) {
            val guildId = context.guildId ?: return
            guildCooldowns[guildId] = until
        }

        override fun getUsageHistory(context: DiscriminatingContext): UsageHistory {
            val guildId = context.guildId ?: return UsageHistoryImpl()
            return guildUsageHistory[guildId] ?: UsageHistoryImpl()
        }

        override fun setUsageHistory(context: DiscriminatingContext, usageHistory: UsageHistory) {
            val guildId = context.guildId ?: return
            guildUsageHistory[guildId] = usageHistory
        }
    };

    internal val userCommandCooldowns: HashMap<Pair<Snowflake, String>, Long> = HashMap()
    internal val userChannelCommandCooldowns: HashMap<Triple<Snowflake, Snowflake, String>, Long> = HashMap()
    internal val userGuildCommandCooldowns: HashMap<Triple<Snowflake, Snowflake, String>, Long> = HashMap()
    internal val userGlobalCooldowns: HashMap<Snowflake, Long> = HashMap()
    internal val userChannelCooldowns: HashMap<Pair<Snowflake, Snowflake>, Long> = HashMap()
    internal val userGuildCooldowns: HashMap<Pair<Snowflake, Snowflake>, Long> = HashMap()
    internal var globalCooldown: Long = 0
    internal val channelCooldowns: HashMap<Snowflake, Long> = HashMap()
    internal val guildCooldowns: HashMap<Snowflake, Long> = HashMap()

    internal val userChannelUsageHistory: MutableMap<Pair<Snowflake, Snowflake>, UsageHistory> = HashMap()
    internal val userChannelCommandUsageHistory: MutableMap<Triple<Snowflake, Snowflake, String>, UsageHistory> =
        HashMap()

    internal val userGuildUsageHistory: MutableMap<Pair<Snowflake, Snowflake>, UsageHistory> = HashMap()
    internal val userGuildCommandUsageHistory: MutableMap<Triple<Snowflake, Snowflake, String>, UsageHistory> =
        HashMap()

    internal val guildUsageHistory: MutableMap<Snowflake, UsageHistory> = HashMap()

    internal val channelUsageHistory: MutableMap<Snowflake, UsageHistory> = HashMap()

    internal val userGlobalHistory: MutableMap<Snowflake, UsageHistory> = HashMap()
    internal val userCommandHistory: MutableMap<Pair<Snowflake, String>, UsageHistory> = HashMap()

    internal var globalHistory: UsageHistory = UsageHistoryImpl()
}
