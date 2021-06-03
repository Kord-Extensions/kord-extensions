package com.kotlindiscord.kord.extensions.utils

import dev.kord.core.entity.Attachment
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import io.ktor.utils.io.jvm.javaio.*
import java.io.File
import java.nio.file.Path
import kotlin.io.path.*

private val client = HttpClient()

/**
 * Download the attachment and return it as a [ByteArray].
 *
 * **Note:** This will load the entire file into memory. If you're just downloading the attachment to a file, use one
 * of the other functions.
 */
public suspend fun Attachment.download(): ByteArray {
    val channel = client.get<ByteReadChannel>(this.url)
    val packet = channel.readRemaining()

    return packet.readBytes()
}

/** Given a [String] representing a file path, download the attachment to the file it points to. **/
public suspend fun Attachment.downloadToFile(path: String): Path = downloadToFile(Path.of(path))

/** Given a [Path] object, download the attachment to the file it points to. **/
@OptIn(ExperimentalPathApi::class)
public suspend fun Attachment.downloadToFile(path: Path): Path {
    if (!path.exists()) {
        path.createDirectories()
        path.deleteExisting()
        path.createFile()
    }

    return downloadToFile(path.toFile())
}

/** Given a [File] object, download the attachment and write it to the given file. **/
@OptIn(ExperimentalPathApi::class)
public suspend fun Attachment.downloadToFile(file: File): Path {
    if (!file.exists()) {
        file.toPath().createFile()
    }

    val channel = client.get<ByteReadChannel>(this.url)

    file.outputStream().use { fileStream ->
        channel.copyTo(fileStream)
    }

    return file.toPath()
}

/** Given a [String] representing a folder path, download the attachment to a file within it. **/
public suspend fun Attachment.downloadToFolder(path: String): Path =
    downloadToFolder(Path.of(path))

/** Given a [Path] representing a folder, download the attachment to a file within it. **/
@OptIn(ExperimentalPathApi::class)
public suspend fun Attachment.downloadToFolder(path: Path): Path =
    downloadToFolder(path.toFile())

/** Given a [File] representing a folder, download the attachment to a file within it. **/
@OptIn(ExperimentalPathApi::class)
public suspend fun Attachment.downloadToFolder(file: File): Path {
    if (!file.exists()) {
        file.toPath().createDirectories()
    }

    val targetFile = File(file, "${this.id.value} - ${this.filename}")
    val channel = client.get<ByteReadChannel>(this.url)

    targetFile.outputStream().use { fileStream ->
        channel.copyTo(fileStream)
    }

    return targetFile.toPath()
}
