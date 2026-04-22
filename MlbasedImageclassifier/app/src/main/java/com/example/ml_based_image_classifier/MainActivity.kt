package com.example.ml_based_image_classifier

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.ml_based_image_classifier.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var tfLiteHelper: TFLiteHelper? = null
    private var selectedBitmap: Bitmap? = null

    // FIX #1: lifecycleScope — auto-cancelled on destroy/rotation, no leak

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        loadModel()
    }

    private fun setupUI() {
        // FIX #3: Both buttons disabled until model finishes loading
        setPickImageEnabled(false)
        setAnalyzeEnabled(false)

        binding.btnPickImage.setOnClickListener {
            checkAndRequestPermissions()
        }

        binding.btnAnalyze.setOnClickListener {
            analyzeFlood()
        }
    }

    private fun loadModel() {
        setStatusText("Loading model…")
        binding.progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            val success = withContext(Dispatchers.IO) {
                try {
                    tfLiteHelper = TFLiteHelper(this@MainActivity)
                    tfLiteHelper?.loadModel() ?: false
                } catch (e: Exception) {
                    e.printStackTrace()
                    false
                }
            }

            binding.progressBar.visibility = View.GONE

            if (success) {
                setStatusText("Model ready")
                setPickImageEnabled(true)   // FIX #3: only enable after model loaded
            } else {
                setStatusText("Failed to load model")
                showToast("Model failed to load. Check assets folder.")
            }
        }
    }

    // FIX #4: Handle runtime permission result — without this, gallery never
    // opens after the user grants permission on Android < 13
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSION_CODE) {
            if (grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED) {
                openGallery()
            } else {
                showToast("Storage permission denied")
            }
        }
    }

    private fun checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ — photo picker needs no storage permission
            openGallery()
        } else {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                openGallery()
            } else {
                requestPermissions(
                    arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                    REQUEST_PERMISSION_CODE
                )
            }
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        imagePickerLauncher.launch(intent)
    }

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri -> handleSelectedImage(uri) }
        }
    }

    private fun handleSelectedImage(uri: Uri) {
        setStatusText("Loading image…")
        binding.progressBar.visibility = View.VISIBLE
        setAnalyzeEnabled(false)

        lifecycleScope.launch {
            val bitmap = withContext(Dispatchers.IO) { uriToBitmap(uri) }

            binding.progressBar.visibility = View.GONE

            if (bitmap != null) {
                selectedBitmap = bitmap
                binding.ivOriginalImage.setImageBitmap(bitmap)
                binding.layoutOriginalSection.visibility = View.VISIBLE
                binding.layoutResultSection.visibility = View.GONE
                resetResultDisplay()
                setAnalyzeEnabled(true)
                setStatusText("Image loaded — tap Analyze")
            } else {
                setStatusText("Failed to load image")
                showToast("Could not read image. Try another file.")
            }
        }
    }

    private fun uriToBitmap(uri: Uri): Bitmap? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                // FIX #2: Force software allocator — avoids Hardware bitmap crash
                // on OEM devices when .copy(ARGB_8888) is called on a hardware bitmap
                val source = ImageDecoder.createSource(contentResolver, uri)
                ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                    decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                }
            } else {
                @Suppress("DEPRECATION")
                val raw = MediaStore.Images.Media.getBitmap(contentResolver, uri)
                raw.copy(Bitmap.Config.ARGB_8888, true).also {
                    if (it !== raw) raw.recycle()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun analyzeFlood() {
        val bitmap = selectedBitmap ?: run {
            showToast("Please select an image first")
            return
        }

        if (tfLiteHelper?.isModelReady() != true) {
            showToast("Model is not ready yet")
            return
        }

        setStatusText("Analyzing…")
        binding.progressBar.visibility = View.VISIBLE
        setAnalyzeEnabled(false)
        setPickImageEnabled(false)
        binding.layoutResultSection.visibility = View.GONE

        lifecycleScope.launch {
            val result = withContext(Dispatchers.IO) {
                try {
                    tfLiteHelper?.runInference(bitmap)
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
            }

            binding.progressBar.visibility = View.GONE
            setAnalyzeEnabled(true)
            setPickImageEnabled(true)

            if (result != null) {
                displayResult(result)
                setStatusText("Analysis complete")
            } else {
                setStatusText("Analysis failed — try again")
                showToast("Inference error. Try again.")
            }
        }
    }

    private fun displayResult(result: FloodResult) {
        // FIX #5: Guard null overlay — fall back to original image
        result.overlayBitmap?.let {
            binding.ivResultImage.setImageBitmap(it)
        } ?: run {
            binding.ivResultImage.setImageBitmap(selectedBitmap)
        }

        // Fade-in result section
        binding.layoutResultSection.alpha = 0f
        binding.layoutResultSection.visibility = View.VISIBLE
        binding.layoutResultSection.animate()
            .alpha(1f)
            .setDuration(350)
            .start()

        val pct = result.floodPercentage
        binding.txtFloodPercentage.text = String.format("%.2f %%", pct)
        binding.txtSeverity.text = result.severity

        binding.progressFlood.progress = pct.toInt()

        val color = FloodResult.getSeverityColor(pct)
        binding.progressFlood.progressTintList =
            android.content.res.ColorStateList.valueOf(color)
        binding.txtSeverity.setTextColor(color)

        // Updated XML splits pixels into two separate TextViews
        binding.txtFloodedPixels.text = formatPixels(result.floodedPixels)
        binding.txtTotalPixels.text = formatPixels(result.totalPixels)

        showToast("Done — ${String.format("%.1f", pct)}% flooded")
    }

    // --- Helpers ---

    private fun formatPixels(count: Int): String {
        return when {
            count >= 1_000_000 -> String.format("%.1fM", count / 1_000_000f)
            count >= 1_000     -> String.format("%.1fK", count / 1_000f)
            else               -> count.toString()
        }
    }

    private fun setStatusText(text: String) {
        binding.txtStatus.text = text
    }

    private fun setAnalyzeEnabled(enabled: Boolean) {
        binding.btnAnalyze.isEnabled = enabled
        binding.btnAnalyze.alpha = if (enabled) 1f else 0.4f
    }

    private fun setPickImageEnabled(enabled: Boolean) {
        binding.btnPickImage.isEnabled = enabled
        binding.btnPickImage.alpha = if (enabled) 1f else 0.4f
    }

    private fun resetResultDisplay() {
        binding.txtFloodPercentage.text = "--.-- %"
        binding.txtSeverity.text = "--"
        binding.progressFlood.progress = 0
        binding.txtFloodedPixels.text = "--"
        binding.txtTotalPixels.text = "--"
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        tfLiteHelper?.close()
        selectedBitmap?.recycle()
        selectedBitmap = null
    }

    companion object {
        private const val REQUEST_PERMISSION_CODE = 1001
    }
}
