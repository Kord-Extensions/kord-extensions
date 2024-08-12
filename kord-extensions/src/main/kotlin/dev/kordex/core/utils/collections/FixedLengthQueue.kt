/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.utils.collections

import dev.kordex.core.utils.collections.serializers.FixedLengthQueueSerializer
import kotlinx.serialization.Serializable
import java.util.*

/**
 * Fixed-length FIFO Queue implementation based on a MutableList.
 *
 * Transparently adds data to the list in reverse (the queue's head is the end of the list), copying and reversing the
 * list when retrieved via [getAll].
 *
 * If you need to be able to tell the difference between a null value and a missing value, consider using Kord's
 * [dev.kord.common.entity.optional.Optional] as the element type.
 *
 * @param maxSize Maximum number of elements in this queue.
 */
@Serializable(with = FixedLengthQueueSerializer::class)
public class FixedLengthQueue<E : Any?>(public val maxSize: Int) : Queue<E> {
	init {
		if (maxSize <= 0) {
			throw IllegalArgumentException("maxSize must be > 0")
		}
	}

	override val size: Int
		get() = data.size

	private val data: MutableList<E> = mutableListOf()

	/**
	 * Add an element to the head of the queue, replacing the element at the tail if [maxSize] has been reached.
	 */
	public fun push(element: E): Boolean {
		if (data.size == maxSize) {
			pop()
		}

		return data.add(element)
	}

	/**
	 * Remove the last element and return it.
	 *
	 * Returns `null` if empty.
	 */
	public fun pop(): E? =
		data.removeFirstOrNull()

	/**
	 * Remove the first element and return it.
	 *
	 * Returns `null` if empty.
	 */
	public fun popFirst(): E? =
		data.removeLastOrNull()

	/**
	 * Retrieve the last element.
	 *
	 * Returns `null` if empty.
	 */
	public fun get(): E? =
		data.firstOrNull()

	/**
	 * Retrieve the first element.
	 *
	 * Returns `null` if empty.
	 */
	public fun getFirst(): E? =
		data.lastOrNull()

	/**
	 * Returns a list of all elements in this queue, in pushed order (newest to oldest).
	 */
	public fun getAll(): List<E> =
		data.reversed()

	override fun add(element: E): Boolean {
		check(data.size == maxSize) { "Queue is full (max size: $maxSize)" }

		return data.add(element)
	}

	override fun addAll(elements: Collection<E>): Boolean =
		if (elements.size >= maxSize) {
			data.clear()

			data.addAll(
				elements.reversed()
					.take(maxSize)
					.reversed()
			)
		} else if (elements.size + data.size > maxSize) {
			val totalSize = elements.size + data.size
			val extraNumber = totalSize - maxSize

			val result = data.addAll(elements)

			repeat(extraNumber) { pop() }

			result
		} else {
			data.addAll(elements)
		}

	override fun clear(): Unit =
		data.clear()

	override fun iterator(): MutableIterator<E> =
		data.iterator()

	override fun remove(): E? =
		data.removeLast()

	override fun isEmpty(): Boolean =
		data.isEmpty()

	override fun poll(): E? =
		data.removeLastOrNull()

	override fun element(): E? =
		data.removeLast()

	override fun peek(): E? =
		data.removeLastOrNull()

	override fun offer(element: E): Boolean =
		push(element)

	override fun containsAll(elements: Collection<E>): Boolean =
		data.containsAll(elements)

	override fun contains(element: E): Boolean =
		data.contains(element)

	override fun retainAll(elements: Collection<E>): Boolean =
		data.retainAll(elements)

	override fun removeAll(elements: Collection<E>): Boolean =
		data.removeAll(elements)

	override fun remove(element: E): Boolean =
		data.remove(element)
}
