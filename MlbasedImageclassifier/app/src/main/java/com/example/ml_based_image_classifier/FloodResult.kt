package com.example.ml_based_image_classifier

import android.graphics.Bitmap

/**
 * Data class to hold the results of flood detection analysis.
 *
 * @property maskBitmap The binary mask bitmap showing flood areas in white
 * @property overlayBitmap The original image with flood areas highlighted in semi-transparent color
 * @property floodPercentage The percentage of the image covered by flood (0-100)
 * @property severity The severity level string (Low, Moderate, High)
 * @property floodedPixels The count of pixels classified as flood
 * @property totalPixels The total pixel count in the analyzed image
 */
data class FloodResult(
    val maskBitmap: Bitmap?,
    val overlayBitmap: Bitmap?,
    val floodPercentage: Double,
    val severity: String,
    val floodedPixels: Int,
    val totalPixels: Int
) {
    companion object {
        /**
         * Determines the severity level based on flood percentage.
         *
         * @param percentage The flood percentage (0-100)
         * @return Severity string: "Low", "Moderate", or "High"
         */
        fun getSeverity(percentage: Double): String {
            return when {
                percentage < 10.0 -> "Low"
                percentage < 30.0 -> "Moderate"
                else -> "High"
            }
        }

        /**
         * Returns the severity color as an integer (ARGB hex value).
         *
         * @param percentage The flood percentage
         * @return Color hex value for the severity level
         */
        fun getSeverityColor(percentage: Double): Int {
            return when {
                percentage < 10.0 -> 0xFF4CAF50.toInt() // Green
                percentage < 30.0 -> 0xFFFF9800.toInt() // Orange
                else -> 0xFFF44336.toInt() // Red
            }
        }
    }
}