package com.maghrebtv.box

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch

class MainViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = ChannelRepository(app)

    var channels by mutableStateOf<Map<Country, List<Channel>>>(emptyMap())
        private set
    var loading by mutableStateOf(true)
        private set

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            loading = true
            channels = Country.entries
                .map { c -> async { c to repo.load(c) } }
                .awaitAll()
                .toMap()
            loading = false
        }
    }
}
