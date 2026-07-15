package com.sukisu.ultra.data.repository

import com.sukisu.ultra.data.model.Author
import com.sukisu.ultra.data.model.ReleaseAsset
import com.sukisu.ultra.data.model.RepoModule
import com.sukisu.ultra.ksuApp
import com.sukisu.ultra.ui.util.isNetworkAvailable
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.atomic.AtomicReference

/**
 * Single source for the online module catalog.
 * Session-level cache avoids re-downloading modules.json for list/detail/update paths.
 */
object ModuleCatalog {

    const val MODULES_URL =
        "https://gitee.com/JT22/MakoSU_ModuleDownload/raw/main/modules.json"

    private const val CACHE_TTL_MS = 5 * 60 * 1000L

    private data class Snapshot(
        val fetchedAtMs: Long,
        val modules: List<RepoModule>,
        val byId: Map<String, RepoModule>,
    )

    private val snapshot = AtomicReference<Snapshot?>(null)

    fun stripTicks(s: String): String {
        val t = s.trim()
        return if (t.startsWith("`") && t.endsWith("`") && t.length >= 2) {
            t.substring(1, t.length - 1)
        } else {
            t
        }
    }

    fun clearCache() {
        snapshot.set(null)
    }

    fun getModules(forceRefresh: Boolean = false): Result<List<RepoModule>> {
        if (!forceRefresh) {
            val cached = snapshot.get()
            if (cached != null && !isExpired(cached)) {
                return Result.success(cached.modules)
            }
        }
        return fetchAndStore()
    }

    fun findModule(moduleId: String, forceRefresh: Boolean = false): RepoModule? {
        if (moduleId.isBlank()) return null
        val list = getModules(forceRefresh).getOrNull() ?: return null
        val snap = snapshot.get()
        if (snap != null) {
            snap.byId[moduleId]?.let { return it }
        }
        return list.firstOrNull { it.moduleId == moduleId }
    }

    private fun isExpired(snap: Snapshot): Boolean {
        return System.currentTimeMillis() - snap.fetchedAtMs > CACHE_TTL_MS
    }

    private fun fetchAndStore(): Result<List<RepoModule>> {
        return runCatching {
            if (!isNetworkAvailable(ksuApp)) {
                throw Exception("Network unavailable")
            }
            val request = Request.Builder().url(MODULES_URL).build()
            ksuApp.okhttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw Exception("Fetch failed: ${response.code}")
                }
                val body = response.body.string()
                val json = JSONArray(body)
                val modules = (0 until json.length()).mapNotNull { idx ->
                    val item = json.optJSONObject(idx) ?: return@mapNotNull null
                    parseRepoModule(item)
                }
                val byId = modules.associateBy { it.moduleId }
                snapshot.set(
                    Snapshot(
                        fetchedAtMs = System.currentTimeMillis(),
                        modules = modules,
                        byId = byId,
                    )
                )
                modules
            }
        }
    }

    private fun parseRepoModule(item: JSONObject): RepoModule? {
        val moduleId = item.optString("moduleId", "")
        if (moduleId.isEmpty()) return null
        val moduleName = item.optString("moduleName", "")
        val authorsArray = item.optJSONArray("authors")
        val authorList = if (authorsArray != null) {
            (0 until authorsArray.length()).mapNotNull { idx ->
                val authorObj = authorsArray.optJSONObject(idx) ?: return@mapNotNull null
                val name = authorObj.optString("name", "").trim()
                val link = stripTicks(authorObj.optString("link", ""))
                if (name.isEmpty()) null else Author(name = name, link = link)
            }
        } else {
            emptyList()
        }
        val authors = if (authorList.isNotEmpty()) {
            authorList.joinToString(", ") { it.name }
        } else {
            item.optString("authors", "")
        }
        val summary = item.optString("summary", "")
        val metamodule = item.optBoolean("metamodule", false)
        val stargazerCount = item.optInt("stargazerCount", 0)

        var latestRelease = ""
        var latestReleaseTime = ""
        var latestVersionCode = 0L
        var latestAsset: ReleaseAsset? = null
        val lr = item.optJSONObject("latestRelease")
        if (lr != null) {
            val lrName = lr.optString("name", lr.optString("version", ""))
            val lrTime = lr.optString("time", "")
            val lrUrl = stripTicks(lr.optString("downloadUrl", ""))
            latestVersionCode = lr.optInt("versionCode", 0).toLong()
            latestRelease = lrName
            latestReleaseTime = lrTime
            if (lrUrl.isNotEmpty()) {
                val fileName = lrUrl.substringAfterLast('/')
                latestAsset = ReleaseAsset(
                    name = fileName,
                    downloadUrl = lrUrl,
                    size = lr.optLong("size", 0L),
                    downloadCount = lr.optInt("downloadCount", 0),
                )
            }
        }

        val repoUrl = item.optString("repoUrl", "").trim().let { stripTicks(it) }

        return RepoModule(
            moduleId = moduleId,
            moduleName = moduleName,
            authors = authors,
            authorList = authorList,
            summary = summary,
            metamodule = metamodule,
            stargazerCount = stargazerCount,
            latestRelease = latestRelease,
            latestReleaseTime = latestReleaseTime,
            latestVersionCode = latestVersionCode,
            latestAsset = latestAsset,
            repoUrl = repoUrl,
        )
    }
}
