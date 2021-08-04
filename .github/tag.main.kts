@file:DependsOn("com.lordcodes.turtle:turtle:0.5.0")

import com.lordcodes.turtle.shellRun
import java.io.File
import kotlin.system.exitProcess

//val webhookUrl = System.getenv("WEBHOOK_URL")
val repo = System.getenv("GITHUB_REPOSITORY")

var githubTag: String = System.getenv("GITHUB_REF") ?: error("No tag found in GITHUB_REF env var")

if (githubTag.contains("/")) {
    githubTag = githubTag.split("/").last()
}

println("Current tag: $githubTag")

if (githubTag.contains("v")) {
    githubTag = githubTag.split("v", limit = 2).last()
}

val tags = shellRun("git", listOf("tag")).trim().split("\n")

val commits = if (tags.size < 2) {
    println("No previous tags, using all branch commits.")

    shellRun("git", listOf("log", "--format=oneline", "--no-color"))
} else {
    val previousTag = tags.takeLast(2).first()

    println("Previous tag: $previousTag")

    shellRun("git", listOf("log", "--format=oneline", "--no-color", "$previousTag..HEAD"))
}.split("\n").map {
    val split = it.split(" ", limit = 2)
    val commit = split.first()
    val message = split.last()

    Pair(commit, message)
}

println("Commits: ${commits.size}")

val commitList = if (commits.size > 10) {
    commits.take(10).joinToString("\n") {
        val (commit, message) = it

        "* [${commit.take(6)}](https://github.com/$repo/commit/$commit): $message"
    } + "\n\n...and ${commits.size - 10} more."
} else {
    commits.joinToString("\n") {
        val (commit, message) = it

        "* [${commit.take(6)}](https://github.com/$repo/commit/$commit): $message"
    }
}

val descFile = File("changes/$githubTag.md")

val description = if (descFile.exists()) {
    descFile.readText(Charsets.UTF_8).trim()
} else {
    "Description file `changes/$githubTag.md` not found - this release will need to be updated later!"
}

val file = File("release.md")

file.writeText(
    "$description\n\n" +
            "---\n\n" +
            "# Commits (${commits.size}) \n\n" +
            commitList
)

print("File written: release.md")

exitProcess(0)
