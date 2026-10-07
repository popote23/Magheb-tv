package com.maghrebtv.box

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = Repository(app)

    var bouquets by mutableStateOf<List<Bouquet>>(emptyList())
        private set
    var loading by mutableStateOf(true)
        private set
    var updating by mutableStateOf(false)
        private set

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            loading = true
            val codes = Catalog.sources

            // 1) affichage rapide depuis le cache
            val cached = withContext(Dispatchers.IO) { codes.associateWith { repo.cached(it) } }
            if (cached.values.any { it.isNotEmpty() }) {
                bouquets = withContext(Dispatchers.Default) { Builder.build(cached) }
                loading = false
            }

            // 2) mise à jour depuis internet
            updating = true
            val fresh = codes.map { c -> async(Dispatchers.IO) { c to repo.download(c) } }
                .awaitAll().toMap()
            bouquets = withContext(Dispatchers.Default) { Builder.build(fresh) }
            updating = false
            loading = false
        }
    }
}
