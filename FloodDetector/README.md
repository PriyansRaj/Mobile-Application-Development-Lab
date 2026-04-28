# FloodWatch

A flood detection Android application built with Kotlin, Material 3, and TensorFlow Lite.

## Features

- **Image Analysis**: Upload satellite/aerial images to detect flood-affected areas using ML
- **Segmentation Results**: Visual overlay showing flood masks with percentage calculations
- **History Tracking**: Store and browse past analysis results with search and filter
- **Customizable Settings**: Theme (light/dark/system), confidence threshold, auto-save

## Tech Stack

- **Language**: Kotlin
- **Min SDK**: 26 | **Target SDK**: 34
- **Architecture**: MVVM (ViewModel + StateFlow)
- **Navigation**: Navigation Component (single Activity, 3 Fragments)
- **Database**: Room
- **ML**: TensorFlow Lite
- **UI**: Material 3 (Material You), View Binding

## Setup Instructions

### 1. Copy ML Model

Copy the TensorFlow Lite model to the assets folder:

```
app/src/main/assets/flood_unet.tflite
```

The model file should be placed at:
```
FloodDetector/app/src/main/assets/flood_unet.tflite
```

### 2. Install Fonts (Optional)

For Plus Jakarta Sans font support:

1. Download Plus Jakarta Sans from [fonts.google.com](https://fonts.google.com/specimen/Plus+Jakarta+Sans)
2. Place the following font files in `app/src/main/res/font/`:
   - `plus_jakarta_sans_regular.ttf`
   - `plus_jakarta_sans_medium.ttf`
   - `plus_jakarta_sans_semibold.ttf`
   - `plus_jakarta_sans_bold.ttf`

If fonts are not installed, the app will use system defaults.

### 3. Build & Run

1. Open the project in Android Studio
2. Sync Gradle (`File > Sync Project with Gradle Files`)
3. Connect a device or start an emulator (API 26+)
4. Run the app (`Shift + F10`)

## Project Structure

```
com.example.floodwatch/
├── ui/
│   ├── MainActivity.kt           # Hosts BottomNavigationView + NavHostFragment
│   ├── HomeFragment.kt           # Upload image, run analysis, show results
│   ├── HistoryFragment.kt        # RecyclerView of past analyses, search + filter
│   └── SettingsFragment.kt        # Theme, confidence, auto-save, clear history
├── adapter/
│   └── HistoryAdapter.kt         # DiffUtil-based RecyclerView adapter
├── viewmodel/
│   ├── HomeViewModel.kt           # Analysis state management
│   └── HistoryViewModel.kt       # History list with filtering
├── db/
│   ├── AppDatabase.kt            # Room singleton
│   ├── AnalysisRecord.kt         # @Entity data class
│   └── AnalysisDao.kt            # Data access object
├── settings/
│   └── SettingsManager.kt        # SharedPreferences wrapper
├── ml/
│   ├── FloodDetector.kt          # TFLite model inference
│   └── SegmentationResult.kt    # Result data class
└── util/
    ├── ImageUtils.kt             # Bitmap operations
    └── SeverityClassifier.kt     # Pure function for severity classification
```

## Severity Classification

| Flood Percentage | Severity |
|------------------|----------|
| < 25%           | Low      |
| 25% - 60%       | Moderate |
| >= 60%          | Severe   |

## Permissions

- `READ_EXTERNAL_STORAGE` (API < 33)
- `READ_MEDIA_IMAGES` (API 33+)

## Build Variants

- **Debug**: For development and testing
- **Release**: For production deployment

## License

MIT License
