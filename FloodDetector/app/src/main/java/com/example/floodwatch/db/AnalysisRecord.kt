package com.example.floodwatch.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "analysis_records")
data class AnalysisRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val timestampMs: Long,
    val floodPercent: Float,
    val floodedPixels: Int,
    val totalPixels: Int,
    val severity: String,
    val originalImagePath: String,
    val resultImagePath: String
)
