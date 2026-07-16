package com.sukisu.ultra.ui.screen.modulerepo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalUriHandler
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sukisu.ultra.ui.navigation3.LocalNavigator
import com.sukisu.ultra.ui.navigation3.Route
import com.sukisu.ultra.ui.screen.flash.FlashIt
import com.sukisu.ultra.ui.viewmodel.ModuleRepoViewModel
import com.sukisu.ultra.ui.viewmodel.ModuleViewModel
import com.sukisu.ultra.ui.util.module.ModuleDetail
import com.sukisu.ultra.ui.util.module.fetchModuleDetail

@Composable
fun ModuleRepoScreen() {
    val navigator = LocalNavigator.current
    val viewModel = viewModel<ModuleRepoViewModel>()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val installedVm = viewModel<ModuleViewModel>()
    val installedUiState by installedVm.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        if (uiState.modules.isEmpty()) {
            viewModel.refresh()
        }
        if (installedUiState.moduleList.isEmpty()) {
            installedVm.fetchModuleList()
        }
    }

    val actions = ModuleRepoActions(
        onBack = { navigator.pop() },
        onRefresh = viewModel::refresh,
        onSearchTextChange = viewModel::updateSearchText,
        onClearSearch = { viewModel.updateSearchText("") },
        onSearchStatusChange = viewModel::updateSearchStatus,
        onSetSortOrder = viewModel::setSortOrder,
        onOpenRepoDetail = { module ->
            val downloadUrl = module.latestAsset?.downloadUrl.orEmpty()
            val assetName = module.latestAsset?.name
                ?: downloadUrl.substringAfterLast('/').ifBlank { "${module.moduleId}.zip" }
            val args = RepoModuleArg(
                moduleId = module.moduleId,
                moduleName = module.moduleName,
                authors = module.authors,
                authorsList = module.authorList.map { AuthorArg(it.name, it.link) },
                summary = module.summary,
                latestRelease = module.latestRelease,
                latestReleaseTime = module.latestReleaseTime,
                downloadUrl = downloadUrl,
                url = module.url,
                homepageUrl = module.homepageUrl,
                sourceUrl = module.sourceUrl,
            )
            navigator.push(Route.ModuleRepoDetail(args))
        },
    )

    ModuleRepoScreenMiuix(uiState, actions)
}

@Composable
fun ModuleRepoDetailScreen(module: RepoModuleArg) {
    val navigator = LocalNavigator.current
    val uriHandler = LocalUriHandler.current
    var detail by remember(module.moduleId) { mutableStateOf<ModuleDetail?>(null) }
    LaunchedEffect(module.moduleId) {
        detail = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            fetchModuleDetail(module.moduleId)
        }
    }
    val loaded = detail
    val sourceUrl = loaded?.sourceUrl.orEmpty().ifBlank { module.sourceUrl }
    val webUrl = loaded?.url.orEmpty().ifBlank { module.url }
    val detailReleases = loaded?.releases?.map { release ->
        ReleaseArg(
            tagName = release.tagName,
            name = release.name,
            publishedAt = release.publishedAt,
            assets = release.assets.map { asset ->
                ReleaseAssetArg(asset.name, asset.downloadUrl, asset.size, asset.downloadCount)
            },
            descriptionHTML = release.descriptionHTML,
        )
    } ?: module.releases
    val readmeHtml = loaded?.readmeHtml?.takeIf { it.isNotBlank() }
        ?: module.summary.takeIf { it.isNotBlank() }
    val displayModule = loaded?.let {
        module.copy(
            moduleName = it.moduleName.ifBlank { module.moduleName },
            authors = it.authors.joinToString(", ") { author -> author.name },
            authorsList = it.authors.map { author -> AuthorArg(author.name, author.link) },
            summary = it.summary,
            latestRelease = it.latestTag,
            latestReleaseTime = it.latestTime,
            url = it.url,
            homepageUrl = it.homepageUrl,
            sourceUrl = it.sourceUrl,
        )
    } ?: module

    val state = ModuleRepoDetailUiState(
        module = displayModule,
        readmeHtml = readmeHtml,
        readmeLoaded = true,
        detailReleases = detailReleases,
        webUrl = webUrl,
        sourceUrl = sourceUrl,
    )
    val actions = ModuleRepoDetailActions(
        onBack = { navigator.pop() },
        onOpenWebUrl = { if (webUrl.isNotEmpty()) uriHandler.openUri(webUrl) },
        onOpenUrl = uriHandler::openUri,
        onInstallModule = { uri -> navigator.push(Route.Flash(FlashIt.FlashModules(listOf(uri)))) },
    )

    ModuleRepoDetailScreenMiuix(state, actions)
}
