package com.sukisu.ultra.ui.util.module

import com.sukisu.ultra.ksuApp
import com.sukisu.ultra.ui.util.isNetworkAvailable
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject

private const val MODULE_DETAIL_URL =
    "https://irislys.github.io/MakoSU_ModuleDownload/module/%s.json"

data class ModuleDetail(
    val readme: String,
    val readmeHtml: String,
    val latestTag: String,
    val latestTime: String,
    val latestAssetName: String?,
    val latestAssetUrl: String?,
    val releases: List<ReleaseInfo>,
    val homepageUrl: String,
    val sourceUrl: String,
    val url: String,
    val moduleName: String,
    val summary: String,
    val authors: List<AuthorInfo>,
)

data class AuthorInfo(val name: String, val link: String)

data class ReleaseInfo(
    val name: String,
    val tagName: String,
    val publishedAt: String,
    val descriptionHTML: String,
    val assets: List<ReleaseAssetInfo>,
)

data class ReleaseAssetInfo(
    val name: String,
    val downloadUrl: String,
    val size: Long,
    val downloadCount: Int,
)

fun sanitizeVersionString(version: String): String {
    return version.replace(Regex("[^a-zA-Z0-9.\\-_]"), "_")
}

fun stripTicks(s: String): String {
    val t = s.trim()
    return if (t.startsWith("`") && t.endsWith("`") && t.length >= 2) {
        t.substring(1, t.length - 1)
    } else {
        t
    }
}

private fun fetchModuleDetailJson(moduleId: String): JSONObject? {
    if (!isNetworkAvailable(ksuApp)) return null
    return runCatching {
            val url = MODULE_DETAIL_URL.format(java.net.URLEncoder.encode(moduleId, "UTF-8"))
            ksuApp.okhttpClient.newCall(Request.Builder().url(url).build()).execute().use { resp ->
            if (!resp.isSuccessful) null else JSONObject(resp.body.string())
        }
    }.getOrNull()
}

private fun parseModuleDetail(item: JSONObject): ModuleDetail {
    val summary = item.optString("summary", "")
    val latestTag = item.optString("latestRelease", "")
    val latestTime = item.optString("latestReleaseTime", "")
    val authors = item.optJSONArray("authors")?.let { array ->
        (0 until array.length()).mapNotNull { index ->
            array.optJSONObject(index)?.let { author ->
                AuthorInfo(author.optString("name", ""), stripTicks(author.optString("link", "")))
            }
        }
    } ?: emptyList()
    val releases = item.optJSONArray("releases")?.let { array ->
        (0 until array.length()).mapNotNull { index -> parseRelease(array.optJSONObject(index)) }
    } ?: emptyList()
    val latestAsset = releases.firstOrNull()?.assets?.firstOrNull()

    return ModuleDetail(
        readme = item.optString("readme", ""),
        readmeHtml = item.optString("readmeHTML", ""),
        latestTag = latestTag,
        latestTime = latestTime,
        latestAssetName = latestAsset?.name,
        latestAssetUrl = latestAsset?.downloadUrl,
        releases = releases,
        homepageUrl = stripTicks(item.optString("homepageUrl", "")),
        sourceUrl = stripTicks(item.optString("sourceUrl", "")),
        url = stripTicks(item.optString("url", "")),
        moduleName = item.optString("moduleName", ""),
        summary = summary,
        authors = authors,
    )
}

private fun parseRelease(item: JSONObject?): ReleaseInfo? {
    if (item == null) return null
    val assets = item.optJSONArray("releaseAssets")?.let { array ->
        (0 until array.length()).mapNotNull { index ->
            array.optJSONObject(index)?.let { asset ->
                val name = asset.optString("name", "")
                val url = stripTicks(asset.optString("downloadUrl", ""))
                if (name.isBlank() || url.isBlank()) null else ReleaseAssetInfo(
                    name = name,
                    downloadUrl = url,
                    size = asset.optLong("size", 0L),
                    downloadCount = asset.optInt("downloadCount", 0),
                )
            }
        }
    } ?: emptyList()
    return ReleaseInfo(
        name = item.optString("name", ""),
        tagName = item.optString("tagName", ""),
        publishedAt = item.optString("publishedAt", ""),
        descriptionHTML = item.optString("descriptionHTML", ""),
        assets = assets,
    )
}

fun fetchReleaseDescriptionHtml(moduleId: String, latestTag: String): String? {
    val detail = fetchModuleDetail(moduleId) ?: return null
    if (latestTag.isBlank() || detail.latestTag == latestTag || detail.latestTag.isBlank()) {
        return detail.readmeHtml.takeIf { it.isNotBlank() }
    }
    return detail.releases
        .firstOrNull { it.tagName == latestTag || it.name == latestTag }
        ?.descriptionHTML
        ?.takeIf { it.isNotBlank() }
        ?: detail.readmeHtml.takeIf { it.isNotBlank() }
}

fun fetchModuleDetail(moduleId: String): ModuleDetail? {
    if (moduleId.isBlank()) return null
    return fetchModuleDetailJson(moduleId)?.let(::parseModuleDetail)
}
