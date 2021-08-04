@file:DependsOn("io.github.rybalkinsd:kohttp:0.12.0")
@file:DependsOn("com.google.code.gson:gson:2.8.6")

import com.google.gson.GsonBuilder
import io.github.rybalkinsd.kohttp.dsl.httpGet
import io.github.rybalkinsd.kohttp.dsl.httpPost
import io.github.rybalkinsd.kohttp.ext.url
import kotlin.system.exitProcess

val gson = GsonBuilder()

val webhookUrl: String = System.getenv("WEBHOOK_URL")

var githubTag: String = System.getenv("GITHUB_REF")
val repo: String = System.getenv("GITHUB_REPOSITORY")

if (githubTag.contains("/")) {
    githubTag = githubTag.split("/").last()
}

println("Current tag: $githubTag")

val apiUrl = "https://api.github.com/repos/$repo/releases/tags/$githubTag"

if (githubTag.contains("v")) {
    githubTag = githubTag.split("v", limit = 2).last()
}

val response = httpGet { url(apiUrl) }
val responseCode = response.code()

if (responseCode >= 400) {
    println("API error: HTTP $responseCode")
    println(response.body()?.string())

    exitProcess(1)
}

val data = gson.create().fromJson<Map<String, *>>(response.body()!!.string(), Map::class.java)

val author = data["author"] as Map<*, *>

val authorAvatar = author["avatar_url"] as String
val authorName = author["login"] as String
val authorUrl = author["html_url"] as String

var releaseBody = (data["body"] as String).replace("\n* ", "\n**»** ").trim()
val releaseName = data["name"] as String
val releaseTime = data["published_at"] as String
val releaseUrl = data["html_url"] as String

if (releaseBody.startsWith("#")) {
    val lines = releaseBody.split("\n").toMutableList()

    lines[0] = lines[0].replaceFirst("#", "**") + "**"
    releaseBody = lines.joinToString("\n")
}

if (releaseBody.contains("---")) {
    releaseBody = releaseBody.split("---", limit = 2).first()
}

val webhook = mapOf(
    "embeds" to listOf(
        mapOf(
            "color" to 7506394,
            "description" to releaseBody,
            "timestamp" to releaseTime.replace("Z", ".000Z"),
            "title" to releaseName,
            "url" to releaseUrl,

            "author" to mapOf(
                "icon_url" to authorAvatar,
                "name" to authorName,
                "url" to authorUrl,
            )
        )
    )
)

val jsonBody = gson.create().toJson(webhook)

val webhookResponse = httpPost {
    url(webhookUrl)

    header {
        "User-Agent" to "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_3) AppleWebKit/537.36 (KHTML, like Gecko) " +
                "Chrome/35.0.1916.47 Safari/537.36"
    }

    body {
        json(jsonBody)
    }
}

val webhookCode = webhookResponse.code()

if (webhookCode >= 400) {
    println("Webhook error: HTTP $webhookCode")
    println(webhookResponse.body()?.string())

    exitProcess(1)
}

exitProcess(0)
