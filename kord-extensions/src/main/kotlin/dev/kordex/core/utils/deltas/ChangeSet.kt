/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.utils.deltas

import dev.kord.common.entity.optional.Optional
import dev.kord.common.entity.optional.optional
import dev.kord.core.entity.VoiceState
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

public class ChangeSet(public val clazz: KClass<*>) {
	private val changes: MutableMap<KProperty<*>, Change<*>> = mutableMapOf()

	@Suppress("UNCHECKED_CAST")
	public operator fun <
		@Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
		@kotlin.internal.OnlyInputTypes
		T : Any?,
		> get(key: KProperty<T>): Change<T> =

		changes[key] as? Change<T>?
			?: throw NoSuchElementException("No such element: $key")

	public operator fun <
		@Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
		@kotlin.internal.OnlyInputTypes
		T : Any?,
		> set(

		key: KProperty<T>,
		value: Change<T>,
	) {
		changes[key] = value
	}

	public data class Change<T : Any?>(
		public val old: Optional<T>,
		public val new: T,

		public val oldState: State,
		public val newState: State,
	)

	public sealed interface State {
		public object Missing : State
		public object Present : State
	}
}

private fun <T : Any> valueFor(obj: Any?, value: T?): Optional<T?> =
	if (obj == null) {
		Optional.Missing.invoke()
	} else {
		value.optional()
	}

public fun VoiceState?.compare(other: VoiceState): ChangeSet {
	val changeSet = ChangeSet(VoiceState::class)

	val oldState = if (this == null) {
		ChangeSet.State.Missing
	} else {
		ChangeSet.State.Present
	}

	changeSet[VoiceState::guildId] =
		ChangeSet.Change(
			valueFor(this, this?.guildId),
			other.guildId,
			oldState,
			ChangeSet.State.Present
		)

	changeSet[VoiceState::channelId] =
		ChangeSet.Change(
			valueFor(this, this?.channelId),
			other.channelId,
			oldState,
			ChangeSet.State.Present
		)

	changeSet[VoiceState::userId] =
		ChangeSet.Change(
			valueFor(this, this?.userId),
			other.userId,
			oldState,
			ChangeSet.State.Present
		)

	changeSet[VoiceState::sessionId] =
		ChangeSet.Change(
			valueFor(this, this?.sessionId),
			other.sessionId,
			oldState,
			ChangeSet.State.Present
		)

	changeSet[VoiceState::isDeafened] =
		ChangeSet.Change(
			valueFor(this, this?.isDeafened),
			other.isDeafened,
			oldState,
			ChangeSet.State.Present
		)

	changeSet[VoiceState::isMuted] =
		ChangeSet.Change(
			valueFor(this, this?.isMuted),
			other.isMuted,
			oldState,
			ChangeSet.State.Present
		)

	changeSet[VoiceState::isSelfDeafened] =
		ChangeSet.Change(
			valueFor(this, this?.isSelfDeafened),
			other.isSelfDeafened,
			oldState,
			ChangeSet.State.Present
		)

	changeSet[VoiceState::isSelfMuted] =
		ChangeSet.Change(
			valueFor(this, this?.isSelfMuted),
			other.isSelfMuted,
			oldState,
			ChangeSet.State.Present
		)

	changeSet[VoiceState::isSelfStreaming] =
		ChangeSet.Change(
			valueFor(this, this?.isSelfStreaming),
			other.isSelfStreaming,
			oldState,
			ChangeSet.State.Present
		)

	changeSet[VoiceState::isSelfVideo] =
		ChangeSet.Change(
			valueFor(this, this?.isSelfVideo),
			other.isSelfVideo,
			oldState,
			ChangeSet.State.Present
		)

	changeSet[VoiceState::isSuppressed] =
		ChangeSet.Change(
			valueFor(this, this?.isSuppressed),
			other.isSuppressed,
			oldState,
			ChangeSet.State.Present
		)

	changeSet[VoiceState::requestToSpeakTimestamp] =
		ChangeSet.Change(
			valueFor(this, this?.requestToSpeakTimestamp),
			other.requestToSpeakTimestamp,
			oldState,
			ChangeSet.State.Present
		)

	return changeSet
}
