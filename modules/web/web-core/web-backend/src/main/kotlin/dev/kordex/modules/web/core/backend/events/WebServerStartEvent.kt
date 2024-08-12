/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.modules.web.core.backend.events

import dev.kordex.core.events.KordExEvent
import dev.kordex.modules.web.core.backend.server.WebServer

public class WebServerStartEvent(public val server: WebServer) : KordExEvent
