package com.gallery.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.gallery.data.local.database.AnalysisStatus
import com.gallery.data.local.database.GalleryDatabase
import com.gallery.data.model.MediaItem
import com.gallery.data.model.toMediaItem
import com.gallery.data.repository.MediaSyncRepository
import com.gallery.worker.GalleryAnalysisWorker
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val database = GalleryDatabase.getDatabase(application)
    private val dao = database.galleryDao()
    private val syncRepository = MediaSyncRepository(application.contentResolver, dao)

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _filterType = MutableStateFlow<String?>(null) // "ALL", "PEOPLE", "PETS", "NATURE", "DOCUMENTS"
    val filterType: StateFlow<String?> = _filterType.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    val mediaItems: StateFlow<List<MediaItem>> = combine(
        _filterType,
        _searchQuery.debounce(300).distinctUntilChanged()
    ) { filter, query ->
        filter to query
    }.flatMapLatest { (filter, query) ->
        if (query.isNotEmpty()) {
            dao.searchMedia(query.trim())
        } else {
            when (filter) {
                "PEOPLE" -> dao.getMediaWithFaces()
                "PETS" -> dao.getMediaByLabel("Dog").combine(dao.getMediaByLabel("Cat")) { dogs, cats -> 
                    (dogs + cats).distinctBy { it.id } 
                }
                "NATURE" -> dao.getMediaByLabel("Nature")
                "DOCUMENTS" -> dao.getMediaWithText()
                else -> dao.getAllMedia()
            }
        }.map { list ->
            list.map { it.toMediaItem() }
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val searchSuggestions: StateFlow<List<String>> = dao.getSearchSuggestions()
        .map { labels ->
            val defaults = listOf("People", "Pets", "Nature", "Screenshots", "Documents")
            (defaults + labels).distinct()
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, listOf("People", "Pets", "Nature", "Screenshots", "Documents"))

    val analysisProgress: StateFlow<String?> = dao.getAllMedia().map { all ->
        val pending = all.count { it.analysisStatus == AnalysisStatus.PENDING }
        if (pending > 0) {
            "Analyzing $pending items..."
        } else {
            null
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, null)

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        syncAndAnalyze()
    }

    fun syncAndAnalyze() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                syncRepository.syncMedia()
                triggerAnalysis()
            } catch (exception: Exception) {
                _error.value = exception.message ?: "Unable to sync media"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun triggerAnalysis() {
        val request = OneTimeWorkRequestBuilder<GalleryAnalysisWorker>().build()
        WorkManager.getInstance(getApplication()).enqueue(request)
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun setFilter(type: String?) {
        _filterType.value = type
    }
}
