package com.example.floodwatch.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.floodwatch.db.AnalysisDao
import com.example.floodwatch.db.AnalysisRecord
import com.example.floodwatch.db.AppDatabase
import com.example.floodwatch.util.ImageUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModel(application: Application) : AndroidViewModel(application) {

    private val dao: AnalysisDao = AppDatabase.getInstance(application).analysisDao()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _severityFilter = MutableStateFlow<String?>(null)
    val severityFilter: StateFlow<String?> = _severityFilter.asStateFlow()

    private val _sortOrder = MutableStateFlow(SortOrder.DESCENDING)

    val analyses: Flow<List<AnalysisRecord>> = combine(
        _searchQuery,
        _severityFilter,
        _sortOrder
    ) { query, severity, sortOrder ->
        Triple(query, severity, sortOrder)
    }.flatMapLatest { (query, severity, _) ->
        when {
            query.isNotEmpty() && severity != null -> dao.searchWithSeverity(query, severity)
            query.isNotEmpty() -> dao.search(query)
            severity != null -> dao.getBySeverity(severity)
            else -> dao.getAll()
        }
    }

    fun search(query: String) {
        _searchQuery.value = query
    }

    fun filterBySeverity(severity: String?) {
        _severityFilter.value = severity
    }

    fun clearFilter() {
        _severityFilter.value = null
        _searchQuery.value = ""
    }

    fun deleteRecord(record: AnalysisRecord) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                ImageUtils.deleteFile(record.originalImagePath)
                ImageUtils.deleteFile(record.resultImagePath)
                dao.delete(record)
            }
        }
    }

    fun deleteAllRecords() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                dao.deleteAll()
            }
        }
    }

    enum class SortOrder {
        ASCENDING,
        DESCENDING
    }
}
