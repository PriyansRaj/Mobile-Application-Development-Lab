package com.example.ml_based_image_classifier

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class TFLiteHelper(private val context: Context) {

    companion object {
        const val MODEL_FILE = "flood_unet.tflite"
        const val THRESHOLD =  0.75f
    }

    private var interpreter: Interpreter? = null
    private var isModelLoaded = false

    private var inputWidth = 256
    private var inputHeight = 256

    fun loadModel(): Boolean {
        return try {
            val modelBuffer = loadModelFile()
            val options = Interpreter.Options()
            options.setNumThreads(4)

            interpreter = Interpreter(modelBuffer, options)
            isModelLoaded = true

            // 🔥 Auto-detect input size
            val inputShape = interpreter!!.getInputTensor(0).shape()
            inputHeight = inputShape[1]
            inputWidth = inputShape[2]

            Log.d("MODEL_DEBUG", getModelInfo())

            true
        } catch (e: Exception) {
            Log.e("MODEL_ERROR", "Model load failed", e)
            false
        }
    }

    private fun loadModelFile(): MappedByteBuffer {
        val fileDescriptor = context.assets.openFd(MODEL_FILE)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        return fileChannel.map(
            FileChannel.MapMode.READ_ONLY,
            fileDescriptor.startOffset,
            fileDescriptor.declaredLength
        )
    }

    fun isModelReady(): Boolean = isModelLoaded && interpreter != null

    fun runInference(bitmap: Bitmap): FloodResult? {
        if (!isModelReady()) return null

        return try {
            val inputBuffer = ImageUtils.prepareInputBuffer(bitmap, inputWidth)

            val outputTensor = interpreter!!.getOutputTensor(0)
            val outputShape = outputTensor.shape()

            Log.d("MODEL_DEBUG", "Output shape: ${outputShape.contentToString()}")

            val mask2D: Array<FloatArray> = when (outputShape.size) {

                // ✅ CASE: [1, H, W]
                3 -> {
                    val output = Array(1) {
                        FloatArray(inputWidth * inputHeight)
                    }

                    interpreter?.run(inputBuffer, output)

                    ImageUtils.flatTo2DMask(
                        output[0],
                        inputWidth,
                        inputHeight
                    )
                }

                // ✅ CASE: [1, H, W, 1]
                4 -> {
                    val output = Array(1) {
                        Array(inputHeight) {
                            Array(inputWidth) {
                                FloatArray(1)
                            }
                        }
                    }

                    interpreter?.run(inputBuffer, output)

                    val mask = Array(inputHeight) { FloatArray(inputWidth) }

                    for (y in 0 until inputHeight) {
                        for (x in 0 until inputWidth) {
                            mask[y][x] = output[0][y][x][0]
                        }
                    }

                    mask
                }

                else -> {
                    Log.e("MODEL_ERROR", "Unsupported output shape: ${outputShape.contentToString()}")
                    return null
                }
            }

            // 🔥 DEBUG MASK RANGE
            var min = Float.MAX_VALUE
            var max = Float.MIN_VALUE

            for (row in mask2D) {
                for (v in row) {
                    if (v < min) min = v
                    if (v > max) max = v
                }
            }

            Log.d("MASK_DEBUG", "Min: $min Max: $max")

            // 📊 Stats
            val (floodedPixels, totalPixels) =
                ImageUtils.calculateFloodStats(mask2D, THRESHOLD)

            val floodPercentage =
                (floodedPixels.toDouble() / totalPixels.toDouble()) * 100.0

            val severity = FloodResult.getSeverity(floodPercentage)

            // 🖼️ Mask bitmap
            val maskBitmap = ImageUtils.floatMaskToBitmap(
                mask2D,
                inputWidth,
                inputHeight,
                THRESHOLD
            )

            // 🌊 BETTER OVERLAY (color based on severity)
            val overlayBitmap = OverlayUtils.createSeverityOverlay(
                bitmap,
                mask2D,
                severity,
                THRESHOLD
            )

            return FloodResult(
                maskBitmap = maskBitmap,
                overlayBitmap = overlayBitmap,
                floodPercentage = floodPercentage,
                severity = severity,
                floodedPixels = floodedPixels,
                totalPixels = totalPixels
            )

        } catch (e: Exception) {
            Log.e("TFLITE_CRASH", "Inference failed", e)
            null
        }
    }

    fun getModelInfo(): String {
        if (!isModelReady()) return "Model not loaded"

        val inputShape = interpreter?.getInputTensor(0)?.shape()
        val outputShape = interpreter?.getOutputTensor(0)?.shape()

        return """
            Model: $MODEL_FILE
            Input shape: ${inputShape?.contentToString()}
            Output shape: ${outputShape?.contentToString()}
        """.trimIndent()
    }

    fun close() {
        interpreter?.close()
        interpreter = null
        isModelLoaded = false
    }
}