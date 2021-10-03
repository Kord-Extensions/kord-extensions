package com.kotlindiscord.kord.extensions.pagination

import com.kotlindiscord.kord.extensions.pagination.builders.PaginatorBuilder
import dev.kord.rest.builder.message.EmbedBuilder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlin.math.ceil

/**
 * Configures this [PaginatorBuilder] to create one page for each [x][pageSize] elements in [items].
 *
 * @param items a [List] containing all the items
 * @param mapper a mapper converting [T] to [String]
 * @param title a function providing the title for the current page
 * @param enumerate whether to include element numbers in entries or not
 * @param additionalPageConfig additional [EmbedBuilder] config, applied to each page
 */
public suspend fun <T> PaginatorBuilder.forList(
    items: List<T>,
    mapper: suspend (T) -> String,
    title: suspend (current: Int, total: Int) -> String,
    pageSize: Int = 8,
    enumerate: Boolean = true,
    additionalPageConfig: suspend EmbedBuilder.() -> Unit = {}
): Unit = forCollection(items.size, { offset, limit ->
    items.subList(offset, offset + limit)
}, mapper, title, pageSize, enumerate, additionalPageConfig)

/**
 * Configures this [PaginatorBuilder] to create one page for each [x][pageSize] elements in [items].
 *
 * **Note**: This requests items from this flow using [drop] and [take]
 *
 * @param total the total amount of items
 * @param items a [Flow] containing all the items
 * @param mapper a mapper converting [T] to [String]
 * @param title a function providing the title for the current page
 * @param enumerate whether to include element numbers in entries or not
 * @param additionalPageConfig additional [EmbedBuilder] config, applied to each page
 */
public suspend fun <T> PaginatorBuilder.forFlow(
    total: Long,
    items: Flow<T>,
    mapper: suspend (T) -> String,
    title: suspend (current: Int, total: Int) -> String,
    pageSize: Int = 8,
    enumerate: Boolean = true,
    additionalPageConfig: suspend EmbedBuilder.() -> Unit = {}
): Unit = forCollection(total.toInt(), { offset, _ ->
    items.drop(offset).take(pageSize).toList()
}, mapper, title, pageSize, enumerate, additionalPageConfig)

/**
 * Adds a collection of type [T] to this paginator.
 *
 * @param size the total size of the collection
 * @param subList a function taking an offset and a limit returning the items in that range
 * @param title a function taking the current page and amount of pages returning a title for the embed
 * @param pageSize the amount of items per page
 * @param enumerate enumerate all the items or not
 * @param additionalPageConfig an [EmbedBuilder] applied to all pages
 */
public suspend fun <T> PaginatorBuilder.forCollection(
    size: Int,
    subList: suspend (offset: Int, limit: Int) -> List<T>,
    mapper: suspend (T) -> String,
    title: suspend (current: Int, total: Int) -> String,
    pageSize: Int = 8,
    enumerate: Boolean = true,
    additionalPageConfig: suspend EmbedBuilder.() -> Unit = {}
) {
    var currentIndexOffset = 0

    repeat(ceil(size / pageSize.toDouble()).toInt()) {
        val items = subList(currentIndexOffset, pageSize)
        addPage(currentIndexOffset, title, items, enumerate, mapper, additionalPageConfig)

        currentIndexOffset += size
    }
}

private fun <T> PaginatorBuilder.addPage(
    myOffset: Int,
    title: suspend (current: Int, end: Int) -> String,
    items: List<T>,
    enumerate: Boolean,
    mapper: suspend (T) -> String,
    additionalPageConfig: suspend EmbedBuilder.() -> Unit
) {
    page {
        this.title = title(myOffset + 1, pages.size)

        description =
            items.mapIndexed { index, item ->
                if (enumerate) {
                    "${index + myOffset + 1}: ${mapper(item)}"
                } else {
                    mapper(item)
                }
            }
                .joinToString("\n")

        additionalPageConfig()
    }
}
