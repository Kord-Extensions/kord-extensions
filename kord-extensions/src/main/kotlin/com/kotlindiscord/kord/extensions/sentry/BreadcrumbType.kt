/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.sentry

/**
 * Sealed class representing all the types of breadcrumbs that Sentry supports.
 *
 * @param name The breadcrumb type name, sent to Sentry
 * @param requiredKeys Array of required keys that must be present in the breadcrumb data for it to be valid, if any
 */
public sealed class BreadcrumbType(public val name: String, public vararg val requiredKeys: String) {
    /** Typically a debug log message. **/
    public object Debug : BreadcrumbType("debug")

    /** The default breadcrumb type. **/
    public object Default : BreadcrumbType("default")

    /** A detected or unhandled error. **/
    public object Error : BreadcrumbType("error")

    /** An HTTP request sent by your bot. **/
    public object HTTP : BreadcrumbType("http")

    /** Information on what's been going on. **/
    public object Info : BreadcrumbType("info")

    /** Navigation action, requiring from/to data keys. **/
    public object Navigation : BreadcrumbType("navigation", "from", "to")

    /** A query made by a user. **/
    public object Query : BreadcrumbType("query")

    /** A tracing event. **/
    public object Transaction : BreadcrumbType("transaction")

    /** A UI interaction. **/
    public object UI : BreadcrumbType("ui")

    /** A user interaction. **/
    public object User : BreadcrumbType("user")
}
