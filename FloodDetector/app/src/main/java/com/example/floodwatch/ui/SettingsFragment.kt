package com.example.floodwatch.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.floodwatch.BuildConfig
import com.example.floodwatch.R
import com.example.floodwatch.databinding.FragmentSettingsBinding
import com.example.floodwatch.settings.SettingsManager
import com.example.floodwatch.viewmodel.HistoryViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val historyViewModel: HistoryViewModel by viewModels()
    private lateinit var settingsManager: SettingsManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        settingsManager = com.example.floodwatch.FloodWatchApp.instance.settingsManager
        setupThemeSettings()
        setupAnalysisSettings()
        setupStorageSettings()
        setupAboutSection()
        updateUI()
    }

    private fun setupThemeSettings() {
        binding.chipGroupTheme.setOnCheckedStateChangeListener { _, checkedIds ->
            when {
                checkedIds.contains(binding.chipSystem.id) -> {
                    settingsManager.themeMode = SettingsManager.THEME_FOLLOW_SYSTEM
                }
                checkedIds.contains(binding.chipLight.id) -> {
                    settingsManager.themeMode = SettingsManager.THEME_LIGHT
                }
                checkedIds.contains(binding.chipDark.id) -> {
                    settingsManager.themeMode = SettingsManager.THEME_DARK
                }
            }
        }
    }

    private fun setupAnalysisSettings() {
        binding.switchAutoSave.setOnCheckedChangeListener { _, isChecked ->
            settingsManager.autoSave = isChecked
        }

        binding.sliderConfidence.addOnChangeListener { _, value, _ ->
            settingsManager.confidenceThreshold = value.toInt()
            binding.tvConfidenceValue.text = "${value.toInt()}%"
        }

        binding.rowOutputQuality.setOnClickListener {
            showOutputQualityDialog()
        }
    }

    private fun setupStorageSettings() {
        binding.rowClearHistory.setOnClickListener {
            showClearHistoryDialog()
        }
    }

    private fun setupAboutSection() {
        binding.tvModelName.text = "flood_unet.tflite"
        binding.tvAppVersion.text = BuildConfig.VERSION_NAME
    }

    private fun updateUI() {
        when (settingsManager.themeMode) {
            SettingsManager.THEME_FOLLOW_SYSTEM -> binding.chipSystem.isChecked = true
            SettingsManager.THEME_LIGHT -> binding.chipLight.isChecked = true
            SettingsManager.THEME_DARK -> binding.chipDark.isChecked = true
        }

        binding.switchAutoSave.isChecked = settingsManager.autoSave
        binding.sliderConfidence.value = settingsManager.confidenceThreshold.toFloat()
        binding.tvConfidenceValue.text = "${settingsManager.confidenceThreshold}%"
        binding.tvOutputQualityValue.text = settingsManager.outputQuality.replaceFirstChar { it.uppercase() }
    }

    private fun showOutputQualityDialog() {
        val options = arrayOf("Low", "Medium", "High")
        val currentIndex = when (settingsManager.outputQuality) {
            SettingsManager.QUALITY_LOW -> 0
            SettingsManager.QUALITY_MEDIUM -> 1
            else -> 2
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Output Quality")
            .setSingleChoiceItems(options, currentIndex) { dialog, which ->
                val quality = when (which) {
                    0 -> SettingsManager.QUALITY_LOW
                    1 -> SettingsManager.QUALITY_MEDIUM
                    else -> SettingsManager.QUALITY_HIGH
                }
                settingsManager.outputQuality = quality
                binding.tvOutputQualityValue.text = options[which]
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showClearHistoryDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Clear History")
            .setMessage("Are you sure you want to delete all analysis records? This action cannot be undone.")
            .setPositiveButton("Clear") { _, _ ->
                historyViewModel.deleteAllRecords()
                Toast.makeText(requireContext(), "History cleared", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
