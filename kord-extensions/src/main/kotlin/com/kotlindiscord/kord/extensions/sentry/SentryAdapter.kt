package com.kotlindiscord.kord.extensions.sentry

import io.sentry.*
import io.sentry.protocol.SentryId

/**
 * A class that wraps the Sentry APIs in order to make them a bit easier to integrate.
 */
public open class SentryAdapter {
    /** Whether Sentry integration has been enabled. **/
    public val enabled: Boolean get() = this._enabled

    private var _enabled: Boolean = false
    private val eventIds: MutableSet<SentryId> = mutableSetOf()

    /**
     * Set up Sentry and enable Sentry integration.
     *
     * This function is a convenience for users that may not want to use a lambda
     * to configure Sentry. As there are *so many options*, not everything is
     * included here - please use [init] if you need to change any
     * other settings.
     *
     * We recommend using keyword arguments for this function. Every single parameter
     * has a default value that should match Sentry's default setup.
     */
    @Suppress("UsePropertyAccessSyntax")
    public fun setup(
        dsn: String? = null,

        diagnosticLevel: SentryLevel? = null,
        distribution: String? = null,
        environment: String? = null,
        release: String? = null,
        sampleRate: Double? = null,
        serverName: String? = null,
        shutdownTimeout: Long? = null,

        attachStacktrace: Boolean = true,
        attachThreads: Boolean = false,
        debug: Boolean = false,
        enableExternalConfiguration: Boolean = true,
        enableNdk: Boolean = true,
        enableScopeSync: Boolean = false,
        enableUncaughtExceptionHandler: Boolean = true,
        sendDefaultPii: Boolean = false,

        inAppExcludes: Array<String> = arrayOf(),
        inAppIncludes: Array<String> = arrayOf(),

        beforeBreadcrumb: ((Breadcrumb, Any?) -> Breadcrumb?)? = null,
        beforeSend: ((SentryEvent?, Any?) -> SentryEvent?)? = null
    ) {
        Sentry.init {
            if (dsn != null) it.dsn = dsn

            if (diagnosticLevel != null) it.setDiagnosticLevel(diagnosticLevel)
            if (distribution != null) it.dist = distribution
            if (environment != null) it.environment = environment
            if (release != null) it.release = release
            if (sampleRate != null) it.sampleRate = sampleRate
            if (serverName != null) it.serverName = serverName
            if (shutdownTimeout != null) it.shutdownTimeout = shutdownTimeout

            it.isAttachStacktrace = attachStacktrace
            it.isAttachThreads = attachThreads
            it.setDebug(debug)
            it.isEnableExternalConfiguration = enableExternalConfiguration
            it.isEnableNdk = enableNdk
            it.isEnableScopeSync = enableScopeSync
            it.enableUncaughtExceptionHandler = enableUncaughtExceptionHandler
            it.isSendDefaultPii = sendDefaultPii

            inAppExcludes.forEach { exclude -> it.addInAppExclude(exclude) }
            inAppIncludes.forEach { include -> it.addInAppInclude(include) }

            if (beforeBreadcrumb != null) it.setBeforeBreadcrumb(beforeBreadcrumb)
            if (beforeSend != null) it.setBeforeSend(beforeSend)
        }

        this._enabled = true
    }

    /**
     * Set up Sentry and enable Sentry integration.
     *
     * This function takes a lambda that matches Sentry's, albeit using a receiver
     * function instead for brevity. Please see the Sentry documentation for
     * information on how to configure it.
     *
     * Use [setup] if you don't want to use a lambda for this.
     */
    public fun init(callback: SentryOptions.() -> Unit) {
        Sentry.init(callback)

        this._enabled = true
    }

    /**
     * Convenience wrapper around the Sentry user feedback API.
     */
    public fun sendFeedback(
        id: SentryId,
        comments: String? = null,
        email: String? = null,
        name: String? = null
    ) {
        if (!enabled) error("Sentry integration has not yet been configured.")

        val feedback = UserFeedback(id)

        if (comments != null) feedback.comments = comments
        if (email != null) feedback.email = email
        if (name != null) feedback.name = name

        Sentry.captureUserFeedback(feedback)
    }

    /**
     * Convenience function for creating a Breadcrumb object.
     */
    public fun createBreadcrumb(
        category: String? = null,
        level: SentryLevel? = null,
        message: String? = null,
        type: String? = null,

        data: Map<String, Any> = mapOf()
    ): Breadcrumb {
        val breadcrumbObj = Breadcrumb()

        if (category != null) breadcrumbObj.category = category
        if (level != null) breadcrumbObj.level = level
        if (message != null) breadcrumbObj.message = message
        if (type != null) breadcrumbObj.type = type

        data.toSortedMap().forEach { (key, value) -> breadcrumbObj.setData(key, value) }

        return breadcrumbObj
    }

    /** Register an event ID that a user may provide feedback for. **/
    public fun addEventId(id: SentryId): Boolean =
        eventIds.add(id)

    /** Given an event ID, check whether it's awaiting feedback. **/
    public fun hasEventId(id: SentryId): Boolean =
        eventIds.contains(id)

    /** Remove an event ID that feedback has already been provided for. **/
    public fun removeEventId(id: SentryId): Boolean =
        eventIds.remove(id)
}
