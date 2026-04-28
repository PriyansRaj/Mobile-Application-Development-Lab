package com.example.floodwatch.ml

import android.graphics.Bitmap

data class SegmentationResult(
    val floodMask: Bitmap,
    val floodedPixels: Int,
    val totalPixels: Int,
    val floodPercent: Float
)
