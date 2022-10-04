package com.kotlindiscord.kord.extensions.usagelimits

import com.kotlindiscord.kord.extensions.usagelimits.cooldowns.CooldownType
import com.kotlindiscord.kord.extensions.usagelimits.ratelimits.RateLimitType

/** Enclosing interface of both [CooldownType] and [RateLimitType]. **/
public interface UsageLimitType : CooldownType, RateLimitType
