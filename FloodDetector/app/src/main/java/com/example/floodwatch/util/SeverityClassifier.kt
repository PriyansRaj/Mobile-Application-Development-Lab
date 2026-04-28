package com.example.floodwatch.util

fun classifySeverity(percent: Float): String = when {
    percent >= 60f -> "Severe"
    percent >= 25f -> "Moderate"
    else -> "Low"
}
