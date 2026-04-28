package com.example.floodwatch.ml

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import com.example.floodwatch.FloodWatchApp
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import kotlin.math.roundToInt

class FloodDetector(private val context: Context) {

    private var interpreter: Interpreter? = null
    private val inputSize = 256
    private val confidenceThreshold: Float
        get() = FloodWatchApp.instance.settingsManager.confidenceThreshold / 100f

    @Synchronized
    fun analyze(bitmap: Bitmap): SegmentationResult {
        if (interpreter == null) {
            interpreter = Interpreter(loadModelFile())
        }

        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, inputSize, inputSize, true)
        val inputBuffer = convertBitmapToByteBuffer(resizedBitmap)
        val outputBuffer = Array(1) { Array(inputSize) { Array(inputSize) { FloatArray(1) } } }

        interpreter?.run(inputBuffer, outputBuffer)

        val maskBitmap = createMaskBitmap(outputBuffer[0], resizedBitmap)
        val floodData = calculateFloodPercentage(outputBuffer[0], inputSize * inputSize)

        return SegmentationResult(
            floodMask = maskBitmap,
            floodedPixels = floodData.first,
            totalPixels = floodData.second,
            floodPercent = floodData.first.toFloat() / floodData.second * 100f
        )
    }

    private fun loadModelFile(): MappedByteBuffer {
        val assetFileDescriptor = context.assets.openFd("flood_unet.tflite")
        val inputStream = FileInputStream(assetFileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = assetFileDescriptor.startOffset
        val declaredLength = assetFileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    private fun convertBitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
        val byteBuffer = ByteBuffer.allocateDirect(4 * inputSize * inputSize * 3)
        byteBuffer.order(ByteOrder.nativeOrder())

        val pixels = IntArray(inputSize * inputSize)
        bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)

        for (pixel in pixels) {
            val r = (pixel shr 16 and 0xFF) / 255f
            val g = (pixel shr 8 and 0xFF) / 255f
            val b = (pixel and 0xFF) / 255f
            byteBuffer.putFloat(r)
            byteBuffer.putFloat(g)
            byteBuffer.putFloat(b)
        }

        return byteBuffer
    }

    private fun createMaskBitmap(output: Array<Array<FloatArray>>, original: Bitmap): Bitmap {
        val maskBitmap = Bitmap.createBitmap(inputSize, inputSize, Bitmap.Config.ARGB_8888)
        val pixels = IntArray(inputSize * inputSize)

        for (y in 0 until inputSize) {
            for (x in 0 until inputSize) {
                val value = output[y][x][0]
                val alpha = if (value > confidenceThreshold) 128 else 0
                pixels[y * inputSize + x] = Color.argb(alpha, 0, 120, 215)
            }
        }

        maskBitmap.setPixels(pixels, 0, inputSize, 0, 0, inputSize, inputSize)
        return maskBitmap
    }

    private fun calculateFloodPercentage(output: Array<Array<FloatArray>>, totalPixels: Int): Pair<Int, Int> {
        var floodedCount = 0
        for (y in 0 until inputSize) {
            for (x in 0 until inputSize) {
                if (output[y][x][0] > confidenceThreshold) {
                    floodedCount++
                }
            }
        }
        return Pair(floodedCount, totalPixels)
    }

    fun close() {
        interpreter?.close()
        interpreter = null
    }
}
