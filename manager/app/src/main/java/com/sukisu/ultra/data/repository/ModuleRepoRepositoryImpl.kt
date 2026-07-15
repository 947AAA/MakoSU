package com.sukisu.ultra.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.sukisu.ultra.data.model.RepoModule

class ModuleRepoRepositoryImpl : ModuleRepoRepository {

    override suspend fun fetchModules(): Result<List<RepoModule>> = withContext(Dispatchers.IO) {
        ModuleCatalog.getModules(forceRefresh = true)
    }
}
