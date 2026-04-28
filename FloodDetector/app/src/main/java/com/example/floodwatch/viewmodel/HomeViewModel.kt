package com.example.floodwatch.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.floodwatch.db.AnalysisDao
import com.example.floodwatch.db.AnalysisRecord
import com.example.floodwatch.db.AppDatabase
import com.example.floodwatch.ml.FloodDetector
import com.example.floodwatch.ml.SegmentationResult
import com.example.floodwatch.settings.SettingsManager
import com.example.floodwatch.util.ImageUtils
import com.example.floodwatch.util.classifySeverity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed class AnalysisState {
    object Idle : AnalysisState()
    object Loading : AnalysisState()
    data class Success(val result: SegmentationResult, val overlayBitmap: Bitmap) : AnalysisState()
    data class Error(val message: String) : AnalysisState()
}

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val floodDetector = FloodDetector(application)
    private val dao: AnalysisDao = AppDatabase.getInstance(application).analysisDao()
    private val settingsManager = SettingsManager(application)

    private val _analyzeState = MutableStateFlow<AnalysisState>(AnalysisState.Idle)
    val analyzeState: StateFlow<AnalysisState> = _analyzeState.asStateFlow()

    private val _currentResult = MutableStateFlow<SegmentationResult?>(null)
    val currentResult: StateFlow<SegmentationResult?> = _currentResult.asStateFlow()

    private val _currentTitle = MutableStateFlow("")
    val title: StateFlow<String> = _currentTitle.asStateFlow()

    fun analyze(bitmap: Bitmap) {
        _analyzeState.value = AnalysisState.Loading
        viewModelScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    floodDetector.analyze(bitmap)
                }
                val overlay = withContext(Dispatchers.Default) {
                    ImageUtils.drawFloodOverlay(bitmap, result.floodMask)
                }
                _currentResult.value = result
                val timestamp = System.currentTimeMillis()
                _currentTitle.value = "Analysis ${java.text.SimpleDateFormat("MMM dd, HH:mm", java.util.Locale.US).format(java.util.Date(timestamp))}"
                _analyzeState.value = AnalysisState.Success(result, overlay)

                if (settingsManager.autoSave) {
                    saveToHistory()
                }
            } catch (e: Exception) {
                _analyzeState.value = AnalysisState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    fun saveToHistory() {
        val result = _currentResult.value ?: return
        val currentTitle = _currentTitle.value.ifEmpty { "Analysis ${System.currentTimeMillis()}" }

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val originalPath = ImageUtils.saveToInternalStorage(
                    getApplication(),
                    result.floodMask,
                    ImageUtils.generateFileName("original")
                )
                val resultPath = ImageUtils.saveToInternalStorage(
                    getApplication(),
                    result.floodMask,
                    ImageUtils.generateFileName("result")
                )

                val record = AnalysisRecord(
                    title = currentTitle,
                    timestampMs = System.currentTimeMillis(),
                    floodPercent = result.floodPercent,
                    floodedPixels = result.floodedPixels,
                    totalPixels = result.totalPixels,
                    severity = classifySeverity(result.floodPercent),
                    originalImagePath = originalPath,
                    resultImagePath = resultPath
                )
                dao.insert(record)
            }
        }
    }

    fun resetState() {
        _analyzeState.value = AnalysisState.Idle
        _currentResult.value = null
        _currentTitle.value = ""
    }

    override fun onCleared() {
        super.onCleared()
        floodDetector.close()
    }
}
