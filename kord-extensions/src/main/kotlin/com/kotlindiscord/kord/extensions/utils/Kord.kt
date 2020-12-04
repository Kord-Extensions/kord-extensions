package com.kotlindiscord.kord.extensions.utils

import dev.kord.core.Kord
import dev.kord.core.entity.User
import dev.kord.core.supplier.EntitySupplyStrategy
import kotlinx.coroutines.flow.Flow

/** Flow containing all [User] objects in the cache. **/
public val Kord.users: Flow<User>
    get() = with(EntitySupplyStrategy.cache).users
