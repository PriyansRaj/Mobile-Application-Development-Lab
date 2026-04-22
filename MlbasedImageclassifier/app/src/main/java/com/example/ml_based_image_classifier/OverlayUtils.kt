package com.example.ml_based_image_classifier

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint

/**
 * Utility object for creating overlay visualizations.
 * Handles blending original images with flood masks to create
 * highlighted overlays showing flood areas.
 */
object OverlayUtils {

    /**
     * Creates an overlay bitmap with flood areas highlighted.
     *
     * @param originalBitmap The original image bitmap
     * @param mask The flood mask as a 2D float array (values 0-1)
     * @param threshold The threshold for determining flood pixels
     * @param overlayColor The color to use for highlighting flood areas (default: semi-transparent blue)
     * @return A bitmap with flood areas highlighted
     */
    fun createOverlay(
        originalBitmap: Bitmap,
        mask: Array<FloatArray>,
        threshold: Float = 1f,
        overlayColor: Int = Color.argb(128, 30, 144, 255) // Semi-transparent dodger blue
    ): Bitmap {
        // Resize mask to match original bitmap dimensions
        val maskWidth = mask[0].size
        val maskHeight = mask.size
        val originalWidth = originalBitmap.width
        val originalHeight = originalBitmap.height

        // Create output bitmap as a copy of the original
        val outputBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(outputBitmap)

        val paint = Paint()
        paint.color = overlayColor
        paint.style = Paint.Style.FILL
        paint.isAntiAlias = false

        // Calculate scaling factors
        val scaleX = originalWidth.toFloat() / maskWidth.toFloat()
        val scaleY = originalHeight.toFloat() / maskHeight.toFloat()

        // Draw overlay on flood pixels
        for (y in 0 until maskHeight) {
            for (x in 0 until maskWidth) {
                if (mask[y][x] > threshold) {
                    // Scale mask coordinates to original image coordinates
                    val scaledX = (x * scaleX).toInt()
                    val scaledY = (y * scaleY).toInt()
                    val scaledEndX = ((x + 1) * scaleX).toInt()
                    val scaledEndY = ((y + 1) * scaleY).toInt()

                    // Draw rectangle on flood area
                    canvas.drawRect(
                        scaledX.toFloat(),
                        scaledY.toFloat(),
                        scaledEndX.toFloat(),
                        scaledEndY.toFloat(),
                        paint
                    )
                }
            }
        }

        return outputBitmap
    }

    /**
     * Creates an overlay bitmap using a pre-computed binary mask bitmap.
     *
     * @param originalBitmap The original image bitmap
     * @param maskBitmap The binary mask bitmap (white = flood, black = non-flood)
     * @param overlayColor The color to use for highlighting (default: semi-transparent blue)
     * @return A bitmap with flood areas highlighted
     */
    fun createOverlayFromMaskBitmap(
        originalBitmap: Bitmap,
        maskBitmap: Bitmap,
        overlayColor: Int = Color.argb(128, 30, 144, 255)
    ): Bitmap {
        // Resize mask to match original if needed
        val resizedMask = if (maskBitmap.width != originalBitmap.width ||
            maskBitmap.height != originalBitmap.height) {
            ImageUtils.resizeBitmap(
                maskBitmap,
                originalBitmap.width,
                originalBitmap.height
            )
        } else {
            maskBitmap
        }

        // Create output bitmap
        val outputBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(outputBitmap)

        val paint = Paint()
        paint.color = overlayColor
        paint.style = Paint.Style.FILL
        paint.isAntiAlias = false

        val width = resizedMask.width
        val height = resizedMask.height
        val pixels = IntArray(width * height)
        resizedMask.getPixels(pixels, 0, width, 0, 0, width, height)

        // Process each pixel
        for (y in 0 until height) {
            for (x in 0 until width) {
                val pixel = pixels[y * width + x]
                // Check if the pixel is white (flood)
                val isWhite = Color.red(pixel) > 128 &&
                    Color.green(pixel) > 128 &&
                    Color.blue(pixel) > 128

                if (isWhite) {
                    canvas.drawPoint(x.toFloat(), y.toFloat(), paint)
                }
            }
        }

        return outputBitmap
    }

    /**
     * Creates a side-by-side comparison bitmap with original and overlay.
     *
     * @param originalBitmap The original image
     * @param overlayBitmap The overlay image
     * @return A combined bitmap showing both images side by side
     */
    fun createComparisonBitmap(
        originalBitmap: Bitmap,
        overlayBitmap: Bitmap
    ): Bitmap {
        val width = originalBitmap.width
        val height = originalBitmap.height

        // Create a combined bitmap (2x width for side by side)
        val combinedWidth = width * 2
        val combinedBitmap = Bitmap.createBitmap(
            combinedWidth,
            height,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(combinedBitmap)

        // Draw original on the left
        canvas.drawBitmap(originalBitmap, 0f, 0f, null)

        // Draw overlay on the right
        canvas.drawBitmap(overlayBitmap, width.toFloat(), 0f, null)

        return combinedBitmap
    }

    /**
     * Creates a color-coded severity overlay.
     * Different severity levels get different overlay colors.
     *
     * @param originalBitmap The original image
     * @param mask The flood mask
     * @param severity The severity level (Low, Moderate, High)
     * @return An overlay bitmap with severity-appropriate coloring
     */
    fun createSeverityOverlay(
        originalBitmap: Bitmap,
        mask: Array<FloatArray>,
        severity: String,
        threshold: Float = 0.5f
    ): Bitmap {
        val overlayColor = when (severity) {
            "Low" -> Color.argb(100, 76, 175, 80)      // Semi-transparent green
            "Moderate" -> Color.argb(120, 255, 152, 0) // Semi-transparent orange
            "High" -> Color.argb(140, 244, 67, 54)     // Semi-transparent red
            else -> Color.argb(128, 30, 144, 255)      // Default blue
        }

        return createOverlay(originalBitmap, mask, threshold, overlayColor)
    }
}