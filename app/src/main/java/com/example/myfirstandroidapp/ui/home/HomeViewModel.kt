package com.gallery.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gallery.data.model.MediaItem
import com.gallery.data.model.MediaType
import com.gallery.data.repository.MediaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository =
        MediaRepository(
            application.contentResolver
        )

    private val _allMedia =
        MutableStateFlow<List<MediaItem>>(emptyList())

    private val _searchQuery =
        MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _filterType =
        MutableStateFlow<MediaType?>(null)
    val filterType: StateFlow<MediaType?> = _filterType.asStateFlow()

    val mediaItems: StateFlow<List<MediaItem>> =
        combine(_allMedia, _searchQuery, _filterType) { media, query, filter ->
            media.filter { item ->
                (filter == null || item.type == filter) &&
                        (query.isEmpty() || item.name.contains(query, ignoreCase = true))
            }
        }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _isLoading =
        MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error =
        MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadMedia()
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun setFilter(type: MediaType?) {
        _filterType.value = type
    }

    fun loadMedia() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                _allMedia.value = repository.getMedia()
            } catch (exception: Exception) {
                _error.value = exception.message ?: "Unable to load media"
            } finally {
                _isLoading.value = false
            }
        }
    }
}