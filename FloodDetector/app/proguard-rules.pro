# Add project specific ProGuard rules here.
# Keep TensorFlow Lite classes
-keep class org.tensorflow.** { *; }
-keepclassmembers class org.tensorflow.** { *; }

# Keep Room entities
-keep class com.example.floodwatch.db.** { *; }

# Keep model classes
-keep class com.example.floodwatch.ml.** { *; }
