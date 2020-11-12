package com.kotlindiscord.kord.extensions.utils

import com.gitlab.kordlib.core.Kord
import com.gitlab.kordlib.core.behavior.channel.createWebhook
import com.gitlab.kordlib.core.entity.User
import com.gitlab.kordlib.core.entity.Webhook
import com.gitlab.kordlib.core.entity.channel.GuildMessageChannel
import com.gitlab.kordlib.core.firstOrNull
import com.gitlab.kordlib.core.supplier.CacheEntitySupplier
import com.gitlab.kordlib.core.supplier.EntitySupplyStrategy
import com.gitlab.kordlib.rest.Image
import kotlinx.coroutines.flow.Flow
import mu.KotlinLogging

val Kord.users: Flow<User>
    get() = with(EntitySupplyStrategy.cache).users
