# Preserve crypto classes
-keep class javax.crypto.** { *; }
-dontwarn javax.crypto.**

# Preserve Material Design classes
-keep class com.google.android.material.** { *; }
-dontwarn com.google.android.material.**

# Preserve line number information for debugging
-keepattributes SourceFile,LineNumberTable
