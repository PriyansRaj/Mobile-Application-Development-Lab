package com.example.ml_based_image_classifier

import android.graphics.Bitmap
import android.graphics.Color
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Utility object for image processing operations.
 * Handles image resizing, normalization, and conversion between formats.
 */
object ImageUtils {

    /**
     * Resizes a bitmap to the specified dimensions.
     *
     * @param bitmap The source bitmap to resize
     * @param targetWidth The desired width
     * @param targetHeight The desired height
     * @return A new resized bitmap
     */
    fun resizeBitmap(bitmap: Bitmap, targetWidth: Int, targetHeight: Int): Bitmap {
        return Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true)
    }

    /**
     * Converts a bitmap to a ByteBuffer suitable for TFLite model input.
     * The output is normalized to [0, 1] range.
     *
     * @param bitmap The source bitmap
     * @param inputWidth The model input width
     * @param inputHeight The model input height
     * @return ByteBuffer containing normalized pixel values
     */
    fun bitmapToByteBuffer(
        bitmap: Bitmap,
        inputWidth: Int,
        inputHeight: Int
    ): ByteBuffer {
        // Create buffer for: batch (1) x height x width x channels (3 for RGB)
        val byteBuffer = ByteBuffer.allocateDirect(4 * 1 * inputWidth * inputHeight * 3)
        byteBuffer.order(ByteOrder.nativeOrder())
        byteBuffer.rewind()

        // Resize if needed
        val resizedBitmap = if (bitmap.width != inputWidth || bitmap.height != inputHeight) {
            resizeBitmap(bitmap, inputWidth, inputHeight)
        } else {
            bitmap
        }

        val pixels = IntArray(inputWidth * inputHeight)
        resizedBitmap.getPixels(pixels, 0, inputWidth, 0, 0, inputWidth, inputHeight)

        for (pixel in pixels) {
            // Normalize RGB values to [0, 1]
            val r = Color.red(pixel) / 255.0f
            val g = Color.green(pixel) / 255.0f
            val b = Color.blue(pixel) / 255.0f

            byteBuffer.putFloat(r)
            byteBuffer.putFloat(g)
            byteBuffer.putFloat(b)
        }

        byteBuffer.rewind()
        return byteBuffer
    }

    /**
     * Converts a float array mask to a displayable grayscale bitmap.
     *
     * @param mask The 2D float array mask with values in [0, 1]
     * @param width The width of the mask
     * @param height The height of the mask
     * @param threshold The threshold for binary classification (default 0.5)
     * @return A bitmap representing the mask
     */
    fun floatMaskToBitmap(
        mask: Array<FloatArray>,
        width: Int,
        height: Int,
        threshold: Float = 0.5f
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        for (y in 0 until height) {
            for (x in 0 until width) {
                val value = mask[y][x]
                val isFlood = value > threshold
                val color = if (isFlood) {
                    // White for flood areas
                    Color.WHITE
                } else {
                    // Black for non-flood areas
                    Color.BLACK
                }
                bitmap.setPixel(x, y, color)
            }
        }

        return bitmap
    }

    /**
     * Converts a 1D float array mask to a 2D float array.
     *
     * @param flatMask The flat float array
     * @param width The width of the 2D mask
     * @param height The height of the 2D mask
     * @return A 2D float array representation of the mask
     */
    fun flatTo2DMask(flatMask: FloatArray, width: Int, height: Int): Array<FloatArray> {
        val mask2D = Array(height) { FloatArray(width) }
        for (i in flatMask.indices) {
            val y = i / width
            val x = i % width
            if (y < height && x < width) {
                mask2D[y][x] = flatMask[i]
            }
        }
        return mask2D
    }

    /**
     * Calculates the flood percentage from a float mask.
     *
     * @param mask The 2D float array mask
     * @param threshold The threshold for binary classification
     * @return A pair of (flooded pixel count, total pixel count)
     */
    fun calculateFloodStats(
        mask: Array<FloatArray>,
        threshold: Float = 0.5f
    ): Pair<Int, Int> {
        var floodedPixels = 0
        val totalPixels = mask.size * mask[0].size

        for (row in mask) {
            for (value in row) {
                if (value > threshold) {
                    floodedPixels++
                }
            }
        }

        return Pair(floodedPixels, totalPixels)
    }

    /**
     * Alternative method to convert bitmap to ByteBuffer with batch dimension.
     * Some models require input shape [1, height, width, 3].
     *
     * @param bitmap The source bitmap
     * @param inputSize The model input size (assumes square input)
     * @return ByteBuffer with shape [1, inputSize, inputSize, 3]
     */
    fun prepareInputBuffer(bitmap: Bitmap, inputSize: Int): ByteBuffer {
        val resizedBitmap = resizeBitmap(bitmap, inputSize, inputSize)

        // ByteBuffer size: 1 batch * height * width * 3 channels * 4 bytes per float
        val buffer = ByteBuffer.allocateDirect(4 * inputSize * inputSize * 3)
        buffer.order(ByteOrder.nativeOrder())

        val pixels = IntArray(inputSize * inputSize)
        resizedBitmap.getPixels(pixels, 0, inputSize, 0, 0, inputSize, inputSize)

        for (pixel in pixels) {
            buffer.putFloat(Color.red(pixel) / 255.0f)
            buffer.putFloat(Color.green(pixel) / 255.0f)
            buffer.putFloat(Color.blue(pixel) / 255.0f)
        }

        buffer.rewind()
        return buffer
    }
}