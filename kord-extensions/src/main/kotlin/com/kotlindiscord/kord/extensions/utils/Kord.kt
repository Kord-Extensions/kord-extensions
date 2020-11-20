package com.kotlindiscord.kord.extensions.utils

import com.gitlab.kordlib.core.Kord
import com.gitlab.kordlib.core.entity.User
import com.gitlab.kordlib.core.supplier.EntitySupplyStrategy
import kotlinx.coroutines.flow.Flow

/** Flow containing all [User] objects in the cache. **/
public val Kord.users: Flow<User>
    get() = with(EntitySupplyStrategy.cache).users
