package com.chronos.app.update

import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

// Reads published builds from this repository's GitHub Releases. Deliberately
// unauthenticated: the repository is public, and asking the user for a token to
// receive their own app's updates would be absurd.
class GitHubReleaseSource(
    private val owner: String = "puneet-swarup",
    private val repo: String = "chronos",
) : ReleaseSource {

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun latestReleases(limit: Int): List<ReleaseInfo> = withContext(Dispatchers.IO) {
        val body = httpGet(RELEASES_URL.format(owner, repo, limit))
        val parsed = json.decodeFromString(ReleaseListSerializer, body)
        parsed
            .filterNot { it.draft }
            .mapNotNull { release -> toReleaseInfo(release) }
    }

    private fun toReleaseInfo(release: GitHubRelease): ReleaseInfo? {
        val (code, name) = UpdatePolicy.parseTag(release.tagName) ?: return null
        val apk = release.assets.firstOrNull { it.name.endsWith(".apk", ignoreCase = true) } ?: return null
        val checksum = release.assets.firstOrNull { it.name.endsWith(".sha256", ignoreCase = true) }
        return ReleaseInfo(
            versionCode = code,
            versionName = name,
            downloadUrl = apk.browserDownloadUrl,
            sha256Url = checksum?.browserDownloadUrl,
            notes = release.body,
            publishedAt = release.publishedAt,
        )
    }

    private fun httpGet(url: String): String {
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            setRequestProperty("Accept", "application/vnd.github+json")
            setRequestProperty("User-Agent", "Chronos-Updater")
            connectTimeout = 15_000
            readTimeout = 20_000
        }
        try {
            val status = connection.responseCode
            if (status != HttpURLConnection.HTTP_OK) {
                throw IOException("GitHub returned HTTP " + status)
            }
            return connection.inputStream.bufferedReader().use { it.readText() }
        } finally {
            connection.disconnect()
        }
    }

    private companion object {
        const val RELEASES_URL = "https://api.github.com/repos/%s/%s/releases?per_page=%d"
    }
}

// Explicit serializer rather than the reified extension: no import ambiguity, and
// the type is visible at the call site.
private val ReleaseListSerializer =
    kotlinx.serialization.builtins.ListSerializer(GitHubRelease.serializer())

@Serializable
internal data class GitHubRelease(
    @SerialName("tag_name") val tagName: String,
    val draft: Boolean = false,
    val body: String? = null,
    @SerialName("published_at") val publishedAt: String? = null,
    val assets: List<GitHubAsset> = emptyList(),
)

@Serializable
internal data class GitHubAsset(
    val name: String,
    @SerialName("browser_download_url") val browserDownloadUrl: String,
    val size: Long = 0,
)
