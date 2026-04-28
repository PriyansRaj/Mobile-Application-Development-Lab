package com.example.floodwatch.ui

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.floodwatch.R
import com.example.floodwatch.databinding.FragmentHomeBinding
import com.example.floodwatch.ml.SegmentationResult
import com.example.floodwatch.util.classifySeverity
import com.example.floodwatch.viewmodel.AnalysisState
import com.example.floodwatch.viewmodel.HomeViewModel
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels()
    private var selectedBitmap: Bitmap? = null

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { loadBitmapFromUri(it) }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
        observeViewModel()
    }

    private fun setupClickListeners() {
        binding.cardUpload.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }

        binding.btnAnalyze.setOnClickListener {
            selectedBitmap?.let { bitmap ->
                viewModel.analyze(bitmap)
            }
        }

        binding.btnSave.setOnClickListener {
            viewModel.saveToHistory()
            Toast.makeText(requireContext(), "Saved to history", Toast.LENGTH_SHORT).show()
            binding.btnSave.visibility = View.GONE
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.analyzeState.collect { state ->
                    updateUI(state)
                }
            }
        }
    }

    private fun updateUI(state: AnalysisState) {
        when (state) {
            is AnalysisState.Idle -> {
                binding.progressBar.visibility = View.GONE
                binding.tvStatus.visibility = View.GONE
                binding.btnAnalyze.isEnabled = selectedBitmap != null
                binding.btnSave.visibility = View.GONE
                binding.resultsSection.visibility = View.GONE
            }
            is AnalysisState.Loading -> {
                binding.progressBar.visibility = View.VISIBLE
                binding.tvStatus.visibility = View.VISIBLE
                binding.tvStatus.text = getString(R.string.analyzing)
                binding.btnAnalyze.isEnabled = false
                binding.btnSave.visibility = View.GONE
                binding.resultsSection.visibility = View.GONE
            }
            is AnalysisState.Success -> {
                binding.progressBar.visibility = View.GONE
                binding.tvStatus.visibility = View.GONE
                binding.btnAnalyze.isEnabled = true
                binding.btnSave.visibility = View.VISIBLE
                binding.resultsSection.visibility = View.VISIBLE
                showResults(state.result)
            }
            is AnalysisState.Error -> {
                binding.progressBar.visibility = View.GONE
                binding.tvStatus.visibility = View.VISIBLE
                binding.tvStatus.text = state.message
                binding.btnAnalyze.isEnabled = true
                binding.btnSave.visibility = View.GONE
                binding.resultsSection.visibility = View.GONE
                Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun showResults(result: SegmentationResult) {
        binding.apply {
            ivResult.setImageBitmap(result.floodMask)
            tvFloodPercent.text = String.format("%.1f%%", result.floodPercent)
            progressFlood.progress = result.floodPercent.toInt()
            tvFloodedPixels.text = getString(R.string.flooded_pixels_format, result.floodedPixels)
            tvTotalPixels.text = getString(R.string.total_pixels_format, result.totalPixels)
            tvSeverity.text = classifySeverity(result.floodPercent)
            tvSeverity.setChipBackgroundColorResource(
                when (classifySeverity(result.floodPercent)) {
                    "Severe" -> R.color.severity_severe
                    "Moderate" -> R.color.severity_moderate
                    else -> R.color.severity_low
                }
            )
        }
    }

    private fun loadBitmapFromUri(uri: Uri) {
        try {
            selectedBitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(requireContext().contentResolver, uri)
                ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                    decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                    decoder.isMutableRequired = true
                }
            } else {
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(requireContext().contentResolver, uri)
            }
            binding.ivThumbnail.setImageBitmap(selectedBitmap)
            binding.btnAnalyze.isEnabled = true
            binding.resultsSection.visibility = View.GONE
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Failed to load image", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
