/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.types

import dev.kordex.core.ArgumentParsingException
import dev.kordex.core.DiscordRelayedException

/**
 * Sealed class representing the reason you're dealing with a failure message right now.
 *
 * If you need to extract the nested throwable, it's recommended that you cast to the required sealed type first to
 * make exception typing easier.
 *
 * @param error Throwable that triggered this failure, if any.
 */
public sealed class FailureReason<E : Throwable>(public val error: E) {
	/** Sealed class representing a basic check failure. **/
	public sealed class BaseCheckFailure<E : DiscordRelayedException>(error: E) :
		FailureReason<E>(error)

	/** Type representing an error thrown during command/component execution. **/
	public class ExecutionError(error: Throwable) :
		FailureReason<Throwable>(error)

	/** Type representing a relayed exception that was thrown during command execution. **/
	public class RelayedFailure(error: DiscordRelayedException) :
		FailureReason<DiscordRelayedException>(error)

	/** Type representing an argument parsing failure, for command types with arguments. **/
	public class ArgumentParsingFailure(error: ArgumentParsingException) :
		FailureReason<ArgumentParsingException>(error)

	/** Type representing a standard "provided" check failure (provided via `check {}`). **/
	public class ProvidedCheckFailure(error: DiscordRelayedException) :
		BaseCheckFailure<DiscordRelayedException>(error)

	/** Type representing a failure caused by the bot having insufficient permissions. **/
	public class OwnPermissionsCheckFailure(error: DiscordRelayedException) :
		BaseCheckFailure<DiscordRelayedException>(error)
}
