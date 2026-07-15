package com.sukisu.ultra.ui.util.module

import com.sukisu.ultra.data.model.RepoModule
import com.sukisu.ultra.data.repository.ModuleCatalog

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
)

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

fun stripTicks(s: String): String = ModuleCatalog.stripTicks(s)

private fun toDetail(module: RepoModule): ModuleDetail {
    val downloadUrl = module.latestAsset?.downloadUrl.orEmpty()
    val assetName = module.latestAsset?.name
        ?: downloadUrl.substringAfterLast('/').takeIf { it.isNotEmpty() }
    val releases = if (downloadUrl.isNotEmpty()) {
        listOf(
            ReleaseInfo(
                name = module.latestRelease,
                tagName = module.latestRelease,
                publishedAt = module.latestReleaseTime,
                descriptionHTML = module.summary,
                assets = listOf(
                    ReleaseAssetInfo(
                        name = assetName ?: "${module.moduleId}.zip",
                        downloadUrl = downloadUrl,
                        size = module.latestAsset?.size ?: 0L,
                        downloadCount = module.latestAsset?.downloadCount ?: 0,
                    )
                ),
            )
        )
    } else {
        emptyList()
    }

    return ModuleDetail(
        readme = module.summary,
        readmeHtml = module.summary,
        latestTag = module.latestRelease,
        latestTime = module.latestReleaseTime,
        latestAssetName = assetName,
        latestAssetUrl = downloadUrl.ifEmpty { null },
        releases = releases,
        homepageUrl = module.repoUrl,
        sourceUrl = module.repoUrl,
        url = module.repoUrl,
    )
}

fun fetchReleaseDescriptionHtml(moduleId: String, latestTag: String): String? {
    val module = ModuleCatalog.findModule(moduleId) ?: return null
    val detail = toDetail(module)
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
    val module = ModuleCatalog.findModule(moduleId) ?: return null
    return toDetail(module)
}
