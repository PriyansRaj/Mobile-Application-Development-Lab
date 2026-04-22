// ════════════════════════════════════════════════
// SettingsManager.kt — SharedPreferences wrapper
// ════════════════════════════════════════════════
package com.example.floodwatch.settings

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit

class SettingsManager(context: Context) {

    private val prefs = context.getSharedPreferences("floodwatch_prefs", Context.MODE_PRIVATE)

    // ── Theme ──────────────────────────────────────────────────────────────
    var themeMode: Int
        get() = prefs.getInt(KEY_THEME, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        set(value) {
            prefs.edit { putInt(KEY_THEME, value) }
            AppCompatDelegate.setDefaultNightMode(value)
        }

    /** Call once at app start to restore saved theme */
    fun applyTheme() = AppCompatDelegate.setDefaultNightMode(themeMode)

    // ── Analysis ───────────────────────────────────────────────────────────
    var autoSave: Boolean
        get() = prefs.getBoolean(KEY_AUTO_SAVE, false)
        set(v) = prefs.edit { putBoolean(KEY_AUTO_SAVE, v) }

    var confidenceThreshold: Int          // 0–100
        get() = prefs.getInt(KEY_THRESHOLD, 50)
        set(v) = prefs.edit { putInt(KEY_THRESHOLD, v) }

    var outputQuality: String             // "Low" | "Medium" | "High"
        get() = prefs.getString(KEY_QUALITY, "High") ?: "High"
        set(v) = prefs.edit { putString(KEY_QUALITY, v) }

    companion object {
        private const val KEY_THEME     = "theme_mode"
        private const val KEY_AUTO_SAVE = "auto_save"
        private const val KEY_THRESHOLD = "confidence_threshold"
        private const val KEY_QUALITY   = "output_quality"
    }
}


// ════════════════════════════════════════════════
// AnalysisRecord.kt — Room entity
// ════════════════════════════════════════════════
package com.example.floodwatch.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "analyses")
data class AnalysisRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,               // e.g. "Analysis #12"
    val timestampMs: Long,           // System.currentTimeMillis()
    val floodPercent: Float,         // 0.0 – 100.0
    val floodedPixels: Int,
    val totalPixels: Int,
    val severity: String,            // "Low" | "Moderate" | "Severe"
    val originalImagePath: String,   // internal storage path
    val resultImagePath: String,
    val confidenceThreshold: Int
)


// ════════════════════════════════════════════════
// AnalysisDao.kt — Room DAO
// ════════════════════════════════════════════════
package com.example.floodwatch.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AnalysisDao {

    @Query("SELECT * FROM analyses ORDER BY timestampMs DESC")
    fun getAll(): Flow<List<AnalysisRecord>>

    @Query("SELECT * FROM analyses WHERE severity = :sev ORDER BY timestampMs DESC")
    fun getBySeverity(sev: String): Flow<List<AnalysisRecord>>

    @Query("SELECT * FROM analyses WHERE title LIKE '%' || :q || '%' ORDER BY timestampMs DESC")
    fun search(q: String): Flow<List<AnalysisRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: AnalysisRecord): Long

    @Delete
    suspend fun delete(record: AnalysisRecord)

    @Query("DELETE FROM analyses")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM analyses")
    suspend fun count(): Int
}


// ════════════════════════════════════════════════
// AppDatabase.kt — Room database
// ════════════════════════════════════════════════
package com.example.floodwatch.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [AnalysisRecord::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun analysisDao(): AnalysisDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "floodwatch.db"
                ).build().also { INSTANCE = it }
            }
    }
}


// ════════════════════════════════════════════════
// SettingsFragment.kt — wires up the Settings UI
// ════════════════════════════════════════════════
package com.example.floodwatch.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.example.floodwatch.R
import com.example.floodwatch.databinding.FragmentSettingsBinding
import com.example.floodwatch.settings.SettingsManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var settings: SettingsManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        settings = SettingsManager(requireContext())
        bindTheme()
        bindAnalysis()
        bindStorage()
    }

    // ── Theme ──────────────────────────────────────────────────────────────
    private fun bindTheme() {
        val isDark = settings.themeMode == AppCompatDelegate.MODE_NIGHT_YES
        binding.switchDarkMode.isChecked = isDark

        // Sync chip selection to stored value
        when (settings.themeMode) {
            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM -> binding.chipSystemTheme.isChecked = true
            AppCompatDelegate.MODE_NIGHT_YES           -> binding.chipDarkTheme.isChecked = true
            AppCompatDelegate.MODE_NIGHT_NO            -> binding.chipLightTheme.isChecked = true
        }

        binding.switchDarkMode.setOnCheckedChangeListener { _, checked ->
            settings.themeMode = if (checked) AppCompatDelegate.MODE_NIGHT_YES
                                 else          AppCompatDelegate.MODE_NIGHT_NO
        }

        binding.chipGroupTheme.setOnCheckedStateChangeListener { _, checkedIds ->
            val mode = when (checkedIds.firstOrNull()) {
                R.id.chipLightTheme  -> AppCompatDelegate.MODE_NIGHT_NO
                R.id.chipDarkTheme   -> AppCompatDelegate.MODE_NIGHT_YES
                else                 -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }
            settings.themeMode = mode
            // keep switch in sync
            binding.switchDarkMode.isChecked = mode == AppCompatDelegate.MODE_NIGHT_YES
        }
    }

    // ── Analysis prefs ─────────────────────────────────────────────────────
    private fun bindAnalysis() {
        binding.switchAutoSave.isChecked = settings.autoSave
        binding.switchAutoSave.setOnCheckedChangeListener { _, v -> settings.autoSave = v }

        binding.sliderThreshold.value = settings.confidenceThreshold.toFloat()
        binding.txtThresholdValue.text = "${settings.confidenceThreshold}%"
        binding.sliderThreshold.addOnChangeListener { _, value, fromUser ->
            if (fromUser) {
                val pct = value.toInt()
                settings.confidenceThreshold = pct
                binding.txtThresholdValue.text = "$pct%"
            }
        }

        binding.txtQualityValue.text = settings.outputQuality
        binding.rowImageQuality.setOnClickListener {
            val options = arrayOf("Low", "Medium", "High")
            val current = options.indexOf(settings.outputQuality).coerceAtLeast(0)
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Output Image Quality")
                .setSingleChoiceItems(options, current) { dialog, idx ->
                    settings.outputQuality = options[idx]
                    binding.txtQualityValue.text = options[idx]
                    dialog.dismiss()
                }
                .show()
        }
    }

    // ── Storage ────────────────────────────────────────────────────────────
    private fun bindStorage() {
        binding.rowClearHistory.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Clear All History?")
                .setMessage("This will permanently delete all saved analyses and their images.")
                .setPositiveButton("Clear") { _, _ ->
                    // viewModel.clearAll()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
