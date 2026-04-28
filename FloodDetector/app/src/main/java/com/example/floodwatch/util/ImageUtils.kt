package com.example.floodwatch.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Color
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

object ImageUtils {

    fun saveToInternalStorage(context: Context, bitmap: Bitmap, filename: String): String {
        val directory = File(context.filesDir, "images")
        if (!directory.exists()) {
            directory.mkdirs()
        }
        val file = File(directory, filename)
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        return file.absolutePath
    }

    fun loadFromInternalStorage(path: String): Bitmap? {
        return try {
            BitmapFactory.decodeFile(path)
        } catch (e: Exception) {
            null
        }
    }

    fun drawFloodOverlay(original: Bitmap, mask: Bitmap): Bitmap {
        val width = original.width
        val height = original.height
        val resultBitmap = original.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(resultBitmap)

        val scaledMask = Bitmap.createScaledBitmap(mask, width, height, true)
        val paint = Paint().apply {
            alpha = 128
        }

        canvas.drawBitmap(scaledMask, 0f, 0f, paint)
        return resultBitmap
    }

    fun generateFileName(prefix: String): String {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        return "${prefix}_$timestamp.png"
    }

    fun resizeBitmap(bitmap: Bitmap, maxSize: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val ratio = width.toFloat() / height.toFloat()

        val newWidth: Int
        val newHeight: Int

        if (width > height) {
            newWidth = maxSize
            newHeight = (maxSize / ratio).roundToInt()
        } else {
            newHeight = maxSize
            newWidth = (maxSize * ratio).roundToInt()
        }

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }

    fun deleteFile(path: String): Boolean {
        return try {
            File(path).delete()
        } catch (e: Exception) {
            false
        }
    }
}
